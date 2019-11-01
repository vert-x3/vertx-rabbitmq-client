package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
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
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

public class RabbitMQClientPublisherTest extends RabbitMQClientTestBase {

  @SuppressWarnings("constantname")
  private static final Logger logger = LoggerFactory.getLogger(RabbitMQClientPublisherTest.class);

  private static final String EXCHANGE_NAME = "RabbitMQClientPublisherTest";
  private static final String QUEUE_NAME = "RabbitMQClientPublisherTestQueue";
  
  static final int getFreePort() {
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
    options.setAutomaticRecoveryEnabled(true);
    options.setConnectionRetries(Integer.MAX_VALUE);
    options.setConnectionRetryDelay(500);
    return options;
  }
  
  static class MessageDefinition {
    final int i;
    final String messageId;
    final String messageBody;

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
    Map<String, RabbitMQMessage> messagesReceived = new HashMap<>(count);
    
    Async latch = ctx.async(count);

    RabbitMQClient consumerClient = connectClient(vertx, config(), cli -> {
      cli.exchangeDeclare(EXCHANGE_NAME, "fanout", true, false);
      cli.queueDeclare(QUEUE_NAME, true, false, false);
      cli.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
      cli.basicConsumer(QUEUE_NAME
              , new QueueOptions().setAutoAck(false)
              , ar -> {
        if (ar.succeeded()) {
          ar.result().handler(m -> {
            synchronized(messages) {
              messagesReceived.put(m.properties().getMessageId(), m);
              cli.basicAck(m.envelope().getDeliveryTag(), false);
              if (messagesReceived.size() == count) {
                latch.complete();
              }
            }
          });
        }
      });          
    });
            
    this.client = connectClient(vertx, config(), cli -> {
      cli.exchangeDeclare(EXCHANGE_NAME, "fanout", true, false);
      cli.queueDeclare(QUEUE_NAME, true, false, false);
      cli.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
    }); 
    
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
        , null
    );
    publisher.getConfirmationStream().handler(c -> {
      synchronized(messages) {
        messages.remove(c.getMessageId());
      }
    });
    
    List<MessageDefinition> messagesCopy;
    synchronized(messages) {
      messagesCopy = new ArrayList<>(messages.values());
    }
    for (MessageDefinition message : messagesCopy) {
      Thread.sleep(1);
      publisher.publish(EXCHANGE_NAME
          , ""
          , new AMQP.BasicProperties.Builder()
              .messageId(message.messageId)
              .build()
          , Buffer.buffer(message.messageBody)
          , null
      );
    }
    while (publisher.getQueueSize() > 0) {
      logger.info("Still got " + publisher.getQueueSize() + " messages in the send queue");
      Thread.sleep(100);
    }
    logger.info("After the publisher has sent everything there remain " + messages.size() + " messages unconfirmed");
    synchronized(messages) {
      messagesCopy = new ArrayList<>(messages.values());
    }
    for (MessageDefinition message : messagesCopy) {
      Thread.sleep(1);
      publisher.publish(EXCHANGE_NAME
          , ""
          , new AMQP.BasicProperties.Builder()
              .messageId(message.messageId)
              .build()
          , Buffer.buffer(message.messageBody)
          , null
      );
    }

    latch.await(10000L);

    client.stop(ctx.asyncAssertSuccess());
  }

  static RabbitMQClient connectClient(Vertx vertx, RabbitMQOptions options, Handler<RabbitMQClient> connectionEstablishedCallback) throws InterruptedException, Exception, TimeoutException, ExecutionException {
    RabbitMQClient client = RabbitMQClient.create(vertx, options);
    if (connectionEstablishedCallback != null) {
      client.addConnectionEstablishedCallback(connectionEstablishedCallback);
    }
    CompletableFuture<Void> latch = new CompletableFuture<>();
    client.start(ar -> {
      if (ar.succeeded()) {
        latch.complete(null);
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    latch.get(10L, TimeUnit.SECONDS);
    return client;
  }

}
