package io.vertx.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.utility.DockerImageName;

import static com.rabbitmq.client.BuiltinExchangeType.FANOUT;
import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class RabbitMQClientBuiltInReconnectTest {

  /**
   * This test verifies that the RabbitMQ Java client reconnection logic works as long as the vertx reconnect attempts is set to zero.
   *
   * The change that makes this work is in the basicConsumer, where the shutdown handler is only set if retries > 0. 
   * Without that change the vertx client shutdown handler is called, 
   * interrupting the java client reconnection logic, even though the vertx reconnection won't work because retries is zero.
   *
   */
  private static final String TEST_EXCHANGE = "RabbitMQClientBuiltInReconnectExchange";
  private static final String TEST_QUEUE = "RabbitMQClientBuiltInReconnectQueue";
  private static final String TEST_MESSAGE = "My Message";
  private static final boolean DEFAULT_RABBITMQ_EXCHANGE_DURABLE = false;
  private static final boolean DEFAULT_RABBITMQ_EXCHANGE_AUTO_DELETE = true;
  private static final String DEFAULT_RABBITMQ_EXCHANGE_TYPE = FANOUT.getType();
  private static final boolean DEFAULT_RABBITMQ_QUEUE_DURABLE = false;
  private static final boolean DEFAULT_RABBITMQ_QUEUE_EXCLUSIVE = true;
  private static final boolean DEFAULT_RABBITMQ_QUEUE_AUTO_DELETE = true;

  public class RabbitMQMessageProducer {

    private final RabbitMQClient rabbitMQClient;

    private final String topic;

    public RabbitMQMessageProducer(RabbitMQClient rabbitMQClient, String topic) {
      this.rabbitMQClient = rabbitMQClient;
      this.topic = topic;
    }

    public Future<Void> setUp() {
      return rabbitMQClient.start()
              .compose(aVoid -> rabbitMQClient.exchangeDeclare(topic,
                      DEFAULT_RABBITMQ_EXCHANGE_TYPE,
                      DEFAULT_RABBITMQ_EXCHANGE_DURABLE,
                      DEFAULT_RABBITMQ_EXCHANGE_AUTO_DELETE
              ))
              .onSuccess(result -> {
                LOGGER.info("Created exchange '{}': {}", topic, result);
              })
              .compose(aVoid -> rabbitMQClient.confirmSelect())
              .onSuccess(result -> LOGGER.info("Confirmation enabled for topic {}", topic))
              .onFailure(throwable -> LOGGER.warn("Failed to create exchange '{}'", topic, throwable));
    }

    public void send(String message) {
      LOGGER.info("Sending message {} to RabbitMQ topic {}", message, topic);
      byte[] messageAsBytes = encode(message);

      rabbitMQClient
              .basicPublish(topic, "", Buffer.buffer(messageAsBytes))
              .compose(aVoid -> rabbitMQClient.waitForConfirms(60000))
              .onSuccess(aVoid -> LOGGER.debug("Published message {} to RabbitMQ topic {}", message, topic))
              .onFailure(throwable -> LOGGER.warn("Failed to publish message {} to RabbitMQ topic {}", message, topic, throwable));
    }

    public Future<Void> close() {
      return rabbitMQClient.stop();
    }
  }

  public static class RabbitMqConsumer {

    private final RabbitMQClient client;

    public RabbitMqConsumer(Vertx vertx) {
      RabbitMQOptions config = new RabbitMQOptions();
      client = RabbitMQClient.create(vertx, config);
    }

    public void listen() {
      client.start().onFailure(ex -> LOGGER.error("Fail to connect to RabbitMQ ", ex));
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQClientBuiltInReconnectTest.class);

  private final AtomicLong producerTimeReference = new AtomicLong();
  private final AtomicReference<RabbitMQMessageProducer> producerReference = new AtomicReference<>();
  private final AtomicReference<RabbitMQClient> consumerReference = new AtomicReference<>();
  private final AtomicReference<Handler<AsyncResult<String>>> handlerReference = new AtomicReference<>();

  private final ObjectMapper mapper;
  private final Network network;
  private final GenericContainer networkedRabbitmq;
  private final ToxiproxyContainer toxiproxy;

  public RabbitMQClientBuiltInReconnectTest() {
    LOGGER.info("Constructing");
    this.mapper = new ObjectMapper();
    this.network = Network.newNetwork();
    this.networkedRabbitmq = new GenericContainer(DockerImageName.parse("rabbitmq:3.8.6-alpine"))
            .withExposedPorts(5672)
            .withNetwork(network);
    this.toxiproxy = new ToxiproxyContainer(DockerImageName.parse("shopify/toxiproxy:2.1.4"))
            .withNetwork(network);
  }

  @Before
  public void setup() {
    LOGGER.info("Starting");
    this.networkedRabbitmq.start();
    this.toxiproxy.start();
  }

  @After
  public void shutdown() {
    this.networkedRabbitmq.stop();
    this.toxiproxy.stop();
    LOGGER.info("Shutdown");
  }

  @Test(timeout = 1 * 60 * 1000L)
  public void testRecoverConnectionOutage(TestContext ctx) throws Exception {
    Vertx vertx = Vertx.vertx();
    createAndStartProducer(vertx);

    Async async = ctx.async();
    Handler<AsyncResult<String>> secondMessageHandler = result1 -> {
      LOGGER.info("Got another message. Connection recovery was successful.");
      vertx.cancelTimer(producerTimeReference.get());
      producerReference.get().close();
      consumerReference.get().stop();
      async.complete();
    };

    Handler<AsyncResult<String>> firstMessageHandler = ar -> {
      vertx.executeBlocking(f -> {
        LOGGER.info("Got a message, Shutdown rabbitmq.");
        networkedRabbitmq.stop();
        f.complete();
      }).compose(a -> {
        return vertx.executeBlocking(f -> {
          LOGGER.info("Restore RabbitMQ and wait for one more message.");
          networkedRabbitmq.start();
          handlerReference.set(secondMessageHandler);
        });
      });
    };

    handlerReference.set(firstMessageHandler);

    Consumer<String> messageProcessor = message -> {
      assertEquals(TEST_MESSAGE, message);
      LOGGER.warn("Received message: {}", message);
      handlerReference.get().handle(Future.succeededFuture());
    };

    createAndStartConsumer(vertx, consumerReference, messageProcessor);

    LOGGER.info("Await message from rabbitmq.");

  }

  private void createAndStartProducer(Vertx vertx) {

    RabbitMQOptions options = getRabbitMQOptions();

    RabbitMQClient rabbitMQClient = RabbitMQClient.create(vertx, options);
    RabbitMQMessageProducer rabbitMQMessageProducer = new RabbitMQMessageProducer(rabbitMQClient, TEST_EXCHANGE);
    rabbitMQMessageProducer.setUp()
            .onFailure(ex -> LOGGER.error("Failed to start consumer: ", ex))
            .onSuccess(v -> {
              LOGGER.info("Setting up periodic send");
              vertx.setPeriodic(1000, unused -> rabbitMQMessageProducer.send(TEST_MESSAGE));
              producerReference.set(rabbitMQMessageProducer);
            }
            );

  }

  private void createAndStartConsumer(Vertx vertx, AtomicReference<RabbitMQClient> consumerReference, Consumer<String> messageProcessor) {
    createConsumer(vertx, messageProcessor)
            .onSuccess(consumerReference::set)
            .onFailure(ex -> LOGGER.error("Failed to start consumer: ", ex));
  }

  private <T> Future<RabbitMQClient> createConsumer(Vertx vertx, Consumer<String> processor) {
    LOGGER.info("Registering RabbitMQ message consumer for exchange {}...", TEST_EXCHANGE);
    RabbitMQOptions rabbitMQOptions = getRabbitMQOptions();
    RabbitMQClient rabbitMQClient = RabbitMQClient.create(vertx, rabbitMQOptions);
    return rabbitMQClient.start()
            .compose(aVoid -> {
              LOGGER.info("Consumer client started");
              return rabbitMQClient.exchangeDeclare(TEST_EXCHANGE
                       , DEFAULT_RABBITMQ_EXCHANGE_TYPE
                       , DEFAULT_RABBITMQ_EXCHANGE_DURABLE
                       , DEFAULT_RABBITMQ_EXCHANGE_AUTO_DELETE
              );
            })
            .compose(aVoid -> {
              LOGGER.info("Exchange declared by consumer");
              return rabbitMQClient.queueDeclare(TEST_QUEUE
                       , DEFAULT_RABBITMQ_QUEUE_DURABLE
                       , DEFAULT_RABBITMQ_QUEUE_EXCLUSIVE
                       , DEFAULT_RABBITMQ_QUEUE_AUTO_DELETE
              );
            })
            .compose(declareResult -> {
              LOGGER.info("Queue declared by consumer");
              return rabbitMQClient.queueBind(TEST_QUEUE, TEST_EXCHANGE, "");
            })
            .compose(aVoid -> {
              LOGGER.info("Queue bound to exchange");
              return rabbitMQClient.basicConsumer(TEST_QUEUE);
            })
            .map(rabbitMQConsumer -> rabbitMQConsumer.handler(rabbitMQMessage -> {
              byte[] value = rabbitMQMessage.body().getBytes();
              String decodedValue = decode(value, String.class);
              LOGGER.debug("Received value {} of type {} on RabbitMQ message queue for exchange {}"
                      , decodedValue
                      , String.class.getName()
                      , TEST_EXCHANGE
              );
              processor.accept(decodedValue);
            }))
            .map(rabbitMQConsumer -> rabbitMQConsumer
            .exceptionHandler(t -> LOGGER.warn("Exception in RabbitMQ consumer", t)))
            .map(rabbitMQConsumer -> rabbitMQClient)
            .onSuccess(rabbitMQConsumer -> LOGGER.debug("Registered RabbitMQ message consumer for exchange {}", TEST_EXCHANGE));
  }

  private RabbitMQOptions getRabbitMQOptions() {
    RabbitMQOptions options = new RabbitMQOptions();

    ToxiproxyContainer.ContainerProxy proxy = toxiproxy.getProxy(networkedRabbitmq, 5672);
    options.setHost(proxy.getContainerIpAddress());
    options.setPort(proxy.getProxyPort());
    options.setConnectionTimeout(1000);
    options.setNetworkRecoveryInterval(1000);
    options.setRequestedHeartbeat(1);
    // Enable Java RabbitMQ client library reconnections
    options.setAutomaticRecoveryEnabled(true);
    // Disable vertx RabbitMQClient reconnections
    options.setReconnectAttempts(0);
    return options;
  }

  public byte[] encode(Object o) {
    try {
      return mapper.writeValueAsBytes(o);
    } catch (Exception e) {
      throw new EncodeException(e.getMessage());
    }
  }

  public <T> T decode(byte[] bytes, Class<T> type) {
    try {
      return mapper.readValue(bytes, type);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage());
    }
  }
}
