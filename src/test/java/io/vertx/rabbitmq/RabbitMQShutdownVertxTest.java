package io.vertx.rabbitmq;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.concurrent.CompletableFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(VertxUnitRunner.class)
public class RabbitMQShutdownVertxTest extends RabbitMQClientTestBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQShutdownVertxTest.class);
  
  @Override
  public RabbitMQOptions config() throws Exception {
    RabbitMQOptions options = super.config();
    options.setUri("amqp://" + rabbitmq.getContainerIpAddress() + ":" + rabbitmq.getMappedPort(5672));
    options.setAutomaticRecoveryEnabled(false);
    options.setReconnectAttempts(Integer.MAX_VALUE);
    options.setReconnectInterval(500);
    return options;
  }

  @Test
  public void testVertxShutdown() throws Throwable {

    CompletableFuture<String> consumerLatch = new CompletableFuture<>();
    
    RabbitMQClient consumerClient = RabbitMQClient.create(vertx, config());
    RabbitMQClient publisherClient = RabbitMQClient.create(vertx, config());
    consumerClient.start()
            .compose(v -> {
              return publisherClient.start();
            })
            .compose(v -> {
              return consumerClient.queueDeclare("RabbitMQShutdownVertxTestQueue", true, true, false);
            })
            .compose(dok -> {
              return consumerClient.exchangeDeclare("RabbitMQShutdownVertxTestExchange", "fanout", true, false);
            })
            .compose(v -> {
              return consumerClient.queueBind("RabbitMQShutdownVertxTestQueue", "RabbitMQShutdownVertxTestExchange", "");
            })
            .compose(v -> {
              return consumerClient.basicConsumer("RabbitMQShutdownVertxTestQueue");
            })
            .compose(consumer -> {
              consumer.handler(msg -> {
                consumerLatch.complete(msg.body().toString());
              });
              
              return publisherClient.basicPublish("RabbitMQShutdownVertxTestExchange", "", Buffer.buffer("Hello"));
            })
            ;    
    assertEquals("Hello", consumerLatch.get());
    
    assertTrue(consumerClient.isConnected());
    assertTrue(publisherClient.isConnected());
  
    LOGGER.info("Stopping vertx");
    vertx.close();
    
    Thread.sleep(500);
    
    assertFalse(consumerClient.isConnected());
    assertFalse(publisherClient.isConnected());
  }

}
