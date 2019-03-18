package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.vertx.test.core.TestUtils.randomAlphaString;

@RunWith(VertxUnitRunner.class)
public class RabbitMQClientTestBase {

  protected RabbitMQClient client;
  protected Channel channel;
  protected Vertx vertx;

  @ClassRule
  public static final RabbitMQContainer rabbitMQ = new RabbitMQContainer();

  protected void connect() throws Exception {
    if (client != null) {
      throw new IllegalStateException("Client already started");
    }
    RabbitMQOptions config = config();
    client = RabbitMQClient.create(vertx, config);
    CompletableFuture<Void> latch = new CompletableFuture<>();
    client.start(ar -> {
      if (ar.succeeded()) {
        latch.complete(null);
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    latch.get(10L, TimeUnit.SECONDS);
    ConnectionFactory factory = new ConnectionFactory();
    if (config.getUri() != null) {
      factory.setUri(config.getUri());
    }
    channel = factory.newConnection().createChannel();
  }

  public RabbitMQOptions config() throws Exception {
    RabbitMQOptions config = new RabbitMQOptions();
    config.setUri("amqp://" + rabbitMQ.getContainerIpAddress() + ":" + rabbitMQ.getMappedPort(5672));
    return config;
  }

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    if (channel != null) {
      channel.close();
    }
    if (vertx != null) {
      vertx.close(ctx.asyncAssertSuccess());
    }
  }

  String setupExchange(TestContext ctx) throws Exception {
    String exchange = randomAlphaString(10);
    AMQP.Exchange.DeclareOk ok = channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, false, true, null);
    ctx.assertNotNull(ok);
    return exchange;
  }

  String setupQueue(TestContext ctx) throws Exception {
    return setupQueue(ctx, null, null);
  }

  String setupQueue(TestContext ctx, Set<String> messages) throws Exception {
    return setupQueue(ctx, messages, null);
  }

  String setupQueue(TestContext ctx, Set<String> messages, String contentType) throws Exception {
    String queue = randomAlphaString(10);
    AMQP.Queue.DeclareOk ok = channel.queueDeclare(queue, false, false, true, null);
    ctx.assertNotNull(ok.getQueue());
    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
      .contentType(contentType).contentEncoding("UTF-8").build();

    if (messages != null) {
      for (String msg : messages) {
        channel.basicPublish("", queue, properties, msg.getBytes(StandardCharsets.UTF_8));
      }
    }
    return queue;
  }

  Set<String> createMessages(int number) {
    Set<String> messages = new HashSet<>();
    for (int i = 0; i < number; i++) {
      messages.add(randomAlphaString(20));
    }
    return messages;
  }
}
