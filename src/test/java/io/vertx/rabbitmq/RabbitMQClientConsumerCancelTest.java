package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.rabbitmq.RabbitMQClientPublisherTest.MessageDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.vertx.rabbitmq.RabbitMQClientPublisherTest.connectClient;

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

    RabbitMQClient consumerClient = connectClient(vertx, config(), cli -> {
      cli.exchangeDeclare(EXCHANGE_NAME, "fanout", true, false);
      cli.queueDeclare(QUEUE_NAME, true, false, false);
      cli.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
      cli.basicConsumer(QUEUE_NAME
              , new QueueOptions().setAutoAck(false)
              , ar -> {
        if (ar.succeeded()) {
          RabbitMQConsumer consumer = ar.result();
          consumer.endHandler(v -> {
            consumerLatch.complete(null);
          });
          consumer.handler(m -> {
            synchronized(messages) {
              messagesReceived.put(m.properties().getMessageId(), m);
              cli.basicAck(m.envelope().getDeliveryTag(), false);
              if (messagesReceived.size() > count / 2) {
                consumer.cancel();
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
    
    CompletableFuture<Void> publisherLatch = new CompletableFuture<>();
    
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
        if (messages.isEmpty()) {
          publisherLatch.complete(null);
        }
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
    publisherLatch.get(1, TimeUnit.MINUTES);
    logger.info("Publisher has sent everything and got confirmations for all of them");

    consumerLatch.get(1, TimeUnit.MINUTES);
    // The consumer should have received at least half the messages, but could have received a few more whilst cancelling.
    ctx.assertTrue(messagesReceived.size() >= count / 2, "Have received " + messagesReceived.size() + " messages, which is fewer than the expected " + count / 2);
    ctx.assertTrue(messagesReceived.size() <= count / 2 + 10, "Have received " + messagesReceived.size() + " messages, which is too many more than the expected " + count / 2);

    client.stop(ctx.asyncAssertSuccess());
  }

}
