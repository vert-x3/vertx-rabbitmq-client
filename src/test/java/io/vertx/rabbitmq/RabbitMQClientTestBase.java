package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.vertx.test.core.TestUtils.randomAlphaString;

@RunWith(VertxUnitRunner.class)
public class RabbitMQClientTestBase {

  protected RabbitMQClient client;
  protected Channel channel;
  protected Vertx vertx;
  protected RabbitMQManagementClient managementClient;

  @ClassRule
  public static final GenericContainer rabbitmq = new FixedHostPortGenericContainer<>("rabbitmq:3.7-management")
    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("my-rabbit"))
    .withExposedPorts(5672, 15672);

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
    config.setUri("amqp://" + rabbitmq.getContainerIpAddress() + ":" + rabbitmq.getMappedPort(5672));
    return config;
  }

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
    managementClient = new RabbitMQManagementClient(vertx, rabbitmq.getContainerIpAddress(),
      rabbitmq.getMappedPort(15672), "guest", "guest");
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
        channel.basicPublish("", queue, properties, msg.getBytes("UTF-8"));
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

  String setupExchange(final TestContext ctx, String type) throws IOException {
    String exchange = randomAlphaString(10);
    channel.exchangeDeclare(exchange, type, true);
    managementClient.getExchange(exchange, ctx.asyncAssertSuccess(exc -> ctx.assertEquals(exc.getName(), exchange)));
    return exchange;
  }

  void setupQueueBinding(TestContext ctx, String queue, String exchange, String routingKey) throws IOException {
    channel.queueBind(queue, exchange, routingKey);
    managementClient.getQueueBindings(queue, exchange, ctx.asyncAssertSuccess(bindings -> ctx.assertTrue(bindings.size() == 1)));
  }

  void setupQueueBinding(TestContext ctx, String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
    channel.queueBind(queue, exchange, routingKey, arguments);
    managementClient.getQueueBindings(queue, exchange, ctx.asyncAssertSuccess(bindings -> ctx.assertTrue(bindings.size() == 1)));
  }

  void setupExchangeBinding(TestContext ctx, String destination, String source, String routingKey) throws IOException {
    channel.exchangeBind(destination, source, routingKey);
    managementClient.getExchangeBindings(destination, source, ctx.asyncAssertSuccess(bindings -> ctx.assertTrue(bindings.size() == 1)));
  }

  void setupExchangeBinding(TestContext ctx, String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
    channel.exchangeBind(destination, source, routingKey, arguments);
    managementClient.getExchangeBindings(destination, source, ctx.asyncAssertSuccess(bindings -> ctx.assertTrue(bindings.size() == 1)));
  }
}
