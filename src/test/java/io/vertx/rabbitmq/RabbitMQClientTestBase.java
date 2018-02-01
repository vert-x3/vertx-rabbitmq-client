package io.vertx.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RabbitMQClientTestBase extends VertxTestBase {

  public static final String CLOUD_AMQP_URI = "amqps://xvjvsrrc:VbuL1atClKt7zVNQha0bnnScbNvGiqgb@moose.rmq.cloudamqp" +
    ".com/xvjvsrrc";

  protected RabbitMQClient client;
  protected Channel channel;

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
    if (!"true".equalsIgnoreCase(System.getProperty("rabbitmq.local"))) {
      // Use CloudAMQP
      config.setUri(CLOUD_AMQP_URI);
    }
    return config;
  }

  @Override
  protected void tearDown() throws Exception {
    if (channel != null) {
      channel.close();
    }
    super.tearDown();
  }
}
