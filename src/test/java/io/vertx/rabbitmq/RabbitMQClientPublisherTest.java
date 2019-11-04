package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.ClassRule;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

public class RabbitMQClientPublisherTest extends RabbitMQClientTestBase {

  @SuppressWarnings("constantname")
  private static final Logger logger = LoggerFactory.getLogger(RabbitMQClientPublisherTest.class);

  private static final String EXCHANGE_NAME = "RabbitMQClientPublisherTest";
  private static final String QUEUE_NAME = "RabbitMQClientPublisherTestQueue";
  
  private static final int getFreePort() {
    try (ServerSocket s = new ServerSocket(0)) {
      return s.getLocalPort();
    } catch(IOException ex) {
      return -1;
    }
  }
  
  @ClassRule
  public static final GenericContainer fixedRabbitmq = new FixedHostPortGenericContainer<>("rabbitmq:3.7")
    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("bouncing-rabbit"))
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
  public void testStopEmpty(TestContext ctx) throws Throwable {
    
    this.client = RabbitMQClient.create(vertx, config());

    RabbitMQPublisher publisher = RabbitMQPublisher.create(vertx
        , client
        , new RabbitMQPublisherOptions()
            .setConnectionRetries(Integer.MAX_VALUE)
            .setConnectionRetryDelay(100)
            .setMaxInternalQueueSize(Integer.MAX_VALUE)
    );
    CompletableFuture startLatch = new CompletableFuture();
    publisher.start(ar -> {
      startLatch.complete(null);
    });
    startLatch.get();
    
    CompletableFuture stopLatch = new CompletableFuture();
    publisher.stop(ar -> {
      stopLatch.complete(null);
    });
    stopLatch.get();
    
  }
  
  @Test
  public void testPublishOverReconnect(TestContext ctx) throws Throwable {

    int count = 1000;
    
    Map<String, MessageDefinition> messages = new HashMap<>(count);
    for (int i = 0; i < count; ++i) {
      String messageId = String.format("NewID-%05d", i);
      messages.put(messageId, new MessageDefinition(i, messageId, "Message " + i));
    }    
    Map<String, RabbitMQMessage> messagesReceived = new HashMap<>(count);
        
    Async receivedEnoughMessagesLatch = ctx.async(count);
    
    // Now that publishers start asynchronously there is a race condition where messages can be published
    // before the connection established callbacks have run, which means that the queue doesn't exist and
    // messages get lost.
    CompletableFuture<Void> letConsumerStartFirst = new CompletableFuture<>();

    AtomicReference<RabbitMQConsumer> consumer = new AtomicReference<>();
    RabbitMQClient consumerClient = RabbitMQClient.create(vertx, config());
    AtomicLong duplicateCount = new AtomicLong();
    prepareClient(consumerClient
        , p -> {
          consumerClient.exchangeDeclare(EXCHANGE_NAME, "fanout", true, false, ar1 -> {
            if (ar1.succeeded()) {
              consumerClient.queueDeclare(QUEUE_NAME, true, false, false, ar2 -> {
                if (ar2.succeeded()) {
                  consumerClient.queueBind(QUEUE_NAME, EXCHANGE_NAME, "", ar3 -> {
                    if (ar3.succeeded()) {
                      p.complete();
                    } else {
                      p.fail(ar3.cause());
                    }
                  });
                } else {
                  p.fail(ar2.cause());
                }
              });
            } else {
              p.fail(ar1.cause());
            }
          });
        }
        , p -> {
          consumerClient.basicConsumer(QUEUE_NAME
                  , new QueueOptions().setAutoAck(false)
                  , ar4 -> {
            if (ar4.succeeded()) {
              letConsumerStartFirst.complete(null);
              consumer.set(ar4.result());
              ar4.result().handler(m -> {
                synchronized(messages) {
                  Object prev = messagesReceived.put(m.properties().getMessageId(), m);
                  if (prev != null) {
                    duplicateCount.incrementAndGet();
                  }
                  // logger.info("Have received " + messagesReceived.size() + " messages with " + duplicateCount.get() + " duplicates");
                  // logger.info("Have received " + messagesReceived.size() + " messages");
                  // List<String> got = new ArrayList<>(messagesReceived.keySet());
                  // Collections.sort(got);
                  // logger.info("Received " + got);
                  consumerClient.basicAck(m.envelope().getDeliveryTag(), false);
                  if (messagesReceived.size() == count) {
                    logger.info("Have received " + messagesReceived.size() + " messages with " + duplicateCount.get() + " duplicates");
                    receivedEnoughMessagesLatch.complete();
                  }
                }
              });
              p.complete(null);
            } else {
              p.completeExceptionally(ar4.cause());
            }
          });
        }
    ); 
    
    letConsumerStartFirst.get(2, TimeUnit.MINUTES);
            
    this.client = RabbitMQClient.create(vertx, config());
    prepareClient(client
        , p -> {
          client.exchangeDeclare(EXCHANGE_NAME, "fanout", true, false, ar1 -> {
            if (ar1.succeeded()) {
              client.queueDeclare(QUEUE_NAME, true, false, false, ar2 -> {
                if (ar2.succeeded()) {
                  client.queueBind(QUEUE_NAME, EXCHANGE_NAME, "", ar3 -> {
                    if (ar3.succeeded()) {
                      p.complete();
                    } else {
                      p.fail(ar3.cause());
                    }
                  });
                } else {
                  p.fail(ar2.cause());
                }
              });
            } else {
              p.fail(ar1.cause());
            }
          });
        }
        , null
    ); 
    
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
    );
    publisher.start(ar -> {
      publisher.getConfirmationStream().handler(c -> {
        synchronized(messages) {
          messages.remove(c.getMessageId());
        }
      });
    });
    
    List<MessageDefinition> messagesCopy;
    synchronized(messages) {
      messagesCopy = new ArrayList<>(messages.values());
      Collections.sort(messagesCopy, (l,r) -> l.messageId.compareTo(r.messageId));
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
    logger.info("Still got " + publisher.getQueueSize() + " messages in the send queue, waiting for that to clear");
    CompletableFuture<Void> emptyPublisherLatch = new CompletableFuture<>();
    publisher.stop(ar -> {
      if (ar.succeeded()) {
        emptyPublisherLatch.complete(null);
      } else {
        emptyPublisherLatch.completeExceptionally(ar.cause());
      }
    });
    emptyPublisherLatch.get();
    publisher.restart();

    synchronized(messages) {
      logger.info("After the publisher has sent everything there remain " + messages.size() + " messages unconfirmed");
      messagesCopy = new ArrayList<>(messages.values());
      Collections.sort(messagesCopy, (l,r) -> l.messageId.compareTo(r.messageId));
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

    logger.info("Waiting up to 20s for the latch");
    receivedEnoughMessagesLatch.await(20000L);
    logger.info("Latched, shutting down");

    logger.info("Shutting down");
    consumer.get().cancel(ar1 -> {
      logger.info("Consumer cancelled");
      consumerClient.stop(ar2 -> {
        logger.info("Consumer client stopped");
        client.stop(ar3 -> {
          logger.info("Producer client stopped");
          ctx.async().complete();
        });
      });
    });
  }

  static void prepareClient(
      RabbitMQClient client
      , Handler<Promise<Void>> connectionEstablishedCallback
      , Handler<CompletableFuture<Void>> startHandler
  ) throws InterruptedException, Exception, TimeoutException, ExecutionException {
    if (connectionEstablishedCallback != null) {
      client.addConnectionEstablishedCallback(connectionEstablishedCallback);
    }
    CompletableFuture<Void> latch = new CompletableFuture<>();
    client.start(ar -> {
      if (ar.succeeded()) {
        if (startHandler != null) {
          startHandler.handle(latch);
        } else {
          latch.complete(null);
        }
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    latch.get(100L, TimeUnit.SECONDS);
  }

}
