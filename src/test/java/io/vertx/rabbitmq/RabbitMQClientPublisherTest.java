package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

public class RabbitMQClientPublisherTest extends RabbitMQClientTestBase {

  @SuppressWarnings("constantname")
  private static final Logger logger = LoggerFactory.getLogger(RabbitMQClientPublisherTest.class);

  private static final int getFreePort() {
    try (ServerSocket s = new ServerSocket(0)) {
      return s.getLocalPort();
    } catch(IOException ex) {
      return -1;
    }
  }
  
  @ClassRule
  public static final GenericContainer fixedRabbitmq = new FixedHostPortGenericContainer<>("rabbitmq:3.7")
    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("my-rabbit"))
    .withFixedExposedPort(getFreePort(), 5672);
  
  @Override
  public RabbitMQOptions config() throws Exception {
    RabbitMQOptions options = super.config();
    options.setUri("amqp://" + fixedRabbitmq.getContainerIpAddress() + ":" + fixedRabbitmq.getMappedPort(5672));
    logger.info("Rabbit URI: {}", options.getUri());
    options.setAutomaticRecoveryEnabled(true);
    options.setConnectionRetries(Integer.MAX_VALUE);
    options.setConnectionRetryDelay(500);
    return options;
  }
  
  private static class MessageDefinition {
    private final int i;
    private final String messageId;
    private final String messageBody;

    public MessageDefinition(int i, String messageId, String messageBody) {
      this.i = i;
      this.messageId = messageId;
      this.messageBody = messageBody;
    }
    
  }
  
  @Test
  public void testPublishOverReconnect(TestContext ctx) throws Throwable {

    int count = 1000;
    
    Map<String, MessageDefinition> messages = new HashMap<>(count);
    for (int i = 0; i < count; ++i) {
      String messageId = "ID-" + i;
      messages.put(messageId, new MessageDefinition(i, messageId, "Message " + i));
    }    
    
    Async latch = ctx.async(count);

    connectClient();
    
    vertx.setTimer(100, l -> {
      vertx.executeBlocking(f -> {
        logger.info("Stopping rabbitmq container");
        fixedRabbitmq.stop();
        logger.info("Starting rabbitmq container");
        fixedRabbitmq.start();
        logger.info("Started rabbitmq container");
        f.complete();
      });
    });

    RabbitMQPublisher publisher = RabbitMQPublisher.create(vertx
        , client
        , new RabbitMQPublisherOptions()
            .setConnectionRetries(Integer.MAX_VALUE)
            .setConnectionRetryDelay(100)
            .setMaxInternalQueueSize(Integer.MAX_VALUE)
        , () -> {
          client.exchangeDeclare("testPublishOverReconnect", "fanout", true, false);
        }
    );
    publisher.getConfirmationStream().handler(c -> {
      logger.info("Confirmation: {} = {}", c.getMessageId(), c.isSucceeded());
      synchronized(messages) {
        messages.remove(c.getMessageId());
        if (messages.size() == 0) {
          latch.complete();
        }
      }
    });
    
    List<MessageDefinition> messagesCopy;
    synchronized(messages) {
      messagesCopy = new ArrayList<>(messages.values());
    }
    for (MessageDefinition message : messagesCopy) {
      Thread.sleep(1);
      logger.info("About to publish {}", message.i);      
      publisher.publish("testPublishOverReconnect"
          , ""
          , new AMQP.BasicProperties.Builder()
              .messageId(message.messageId)
              .build()
          , Buffer.buffer(message.messageBody)
          , null
      );
    }
    while (publisher.getQueueSize() > 0) {
      logger.info("Still got {} messages in the send queue", publisher.getQueueSize());
      Thread.sleep(100);
    }
    logger.info("After the publisher has sent everything there remain {} messages unconfirmed", messages.size());
    synchronized(messages) {
      messagesCopy = new ArrayList<>(messages.values());
    }
    for (MessageDefinition message : messagesCopy) {
      Thread.sleep(1);
      logger.info("About to publish {}", message.i);      
      publisher.publish("testPublishOverReconnect"
          , ""
          , new AMQP.BasicProperties.Builder()
              .messageId(message.messageId)
              .build()
          , Buffer.buffer(message.messageBody)
          , null
      );
    }
    

    
    latch.await(100000000L);

    client.stop(ctx.asyncAssertSuccess());
  }

  protected void connectClient() throws InterruptedException, Exception, TimeoutException, ExecutionException {
    client = RabbitMQClient.create(vertx, config());
    CompletableFuture<Void> latch = new CompletableFuture<>();
    client.start(ar -> {
      if (ar.succeeded()) {
        latch.complete(null);
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    latch.get(10L, TimeUnit.SECONDS);
  }

}
