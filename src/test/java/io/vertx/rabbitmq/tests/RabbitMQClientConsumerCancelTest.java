package io.vertx.rabbitmq.tests;

import com.rabbitmq.client.AMQP;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.rabbitmq.*;
import io.vertx.rabbitmq.tests.RabbitMQClientPublisherTest.MessageDefinition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.rabbitmq.tests.RabbitMQClientPublisherTest.prepareClient;

/**
 * Test to ensure that the newly added shutdown handler for the consumer does not obstruct a normal consumer cancel.
 * @author njt
 */
public class RabbitMQClientConsumerCancelTest extends RabbitMQClientTestBase {

  @SuppressWarnings("constantname")
  private static final Logger logger = LoggerFactory.getLogger(RabbitMQClientConsumerCancelTest.class);

  private static final String EXCHANGE_NAME = "RabbitMQClientConsumerCancelTest";
  private static final String QUEUE_NAME = "RabbitMQClientConsumerCancelTestQueue";

  @Test
  public void testConsumerShutdown(TestContext ctx) throws Throwable {

    int count = 1000;

    Map<String, MessageDefinition> messages = new HashMap<>(count);
    for (int i = 0; i < count; ++i) {
      String messageId = "ID-" + i;
      messages.put(messageId, new MessageDefinition(i, messageId, "Message " + i));
    }
    Map<String, RabbitMQMessage> messagesReceived = new HashMap<>(count);

    CompletableFuture<Void> consumerLatch = new CompletableFuture<>();

    // Now that publishers start asynchronously there is a race condition where messages can be published
    // before the connection established callbacks have run, which means that the queue doesn't exist and
    // messages get lost.
    CompletableFuture<Void> letConsumerStartFirst = new CompletableFuture<>();

    AtomicReference<RabbitMQConsumer> consumer = new AtomicReference<>();
    RabbitMQClient consumerClient = RabbitMQClient.create(vertx, config());
    prepareClient(consumerClient
        , p -> {
        consumerClient.queueDeclare(QUEUE_NAME, true, false, false)
          .onComplete(ar1 -> {
            if (ar1.succeeded()) {
              consumerClient.exchangeDeclare(EXCHANGE_NAME, "fanout", true, false).onComplete(ar2 -> {
                if (ar2.succeeded()) {
                  consumerClient.queueBind(QUEUE_NAME, EXCHANGE_NAME, "").onComplete(ar3 -> {
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
          ).onComplete(ar4 -> {
            if (ar4.succeeded()) {
              letConsumerStartFirst.complete(null);
              consumer.set(ar4.result());
              ar4.result().handler(m -> {
                synchronized(messages) {
                  messagesReceived.put(m.properties().getMessageId(), m);
                  consumerClient.basicAck(m.envelope().getDeliveryTag(), false);
                  if (messagesReceived.size() > count / 2) {
                    consumer.get().cancel();
                    consumerLatch.complete(null);
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
        client.queueDeclare(QUEUE_NAME, true, false, false).onComplete(ar1 -> {
            if (ar1.succeeded()) {
              client.exchangeDeclare(EXCHANGE_NAME, "fanout", true, false).onComplete(ar2 -> {
                if (ar2.succeeded()) {
                  client.queueBind(QUEUE_NAME, EXCHANGE_NAME, "").onComplete(ar3 -> {
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

    CompletableFuture<Void> publisherLatch = new CompletableFuture<>();

    RabbitMQPublisher publisher = RabbitMQPublisher.create(vertx
        , client
        , new RabbitMQPublisherOptions()
            .setReconnectAttempts(Integer.MAX_VALUE)
            .setReconnectInterval(100)
            .setMaxInternalQueueSize(Integer.MAX_VALUE)
    );
    publisher.start().onComplete(ar -> {
      publisher.getConfirmationStream().handler(c -> {
        synchronized(messages) {
          messages.remove(c.getMessageId());
          if (messages.isEmpty()) {
            publisherLatch.complete(null);
          }
        }
      });
    });

    List<MessageDefinition> messagesCopy;
    synchronized(messages) {
      messagesCopy = new ArrayList<>(messages.values());
      messagesCopy.sort(Comparator.comparing(l -> l.messageId));
    }
    for (MessageDefinition message : messagesCopy) {
      Thread.sleep(1);
      publisher.publish(EXCHANGE_NAME
          , ""
          , new AMQP.BasicProperties.Builder()
              .messageId(message.messageId)
              .build()
          , Buffer.buffer(message.messageBody));
    }
    publisherLatch.get(1, TimeUnit.MINUTES);
    logger.info("Publisher has sent everything and got confirmations for all of them");

    consumerLatch.get(1, TimeUnit.MINUTES);
    // The consumer should have received at least half the messages, but could have received a few more whilst cancelling.
    ctx.assertTrue(messagesReceived.size() >= count / 2, "Have received " + messagesReceived.size() + " messages, which is fewer than the expected " + count / 2);
    ctx.assertTrue(messagesReceived.size() <= count / 2 + 10, "Have received " + messagesReceived.size() + " messages, which is too many more than the expected " + count / 2);

    consumerClient.stop().onComplete(ar2 -> {
      logger.info("Consumer client stopped");
      client.stop().onComplete(ar3 -> {
        logger.info("Producer client stopped");
        ctx.async().complete();
      });
    });
  }

}
