package io.vertx.rabbitmq.tests;

import com.rabbitmq.client.AMQP;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.rabbitmq.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

@RunWith(VertxUnitRunner.class)
public class RabbitMQClientPublisherTest {

  @SuppressWarnings("constantname")
  private static final Logger logger = LoggerFactory.getLogger(RabbitMQClientPublisherTest.class);

  private String exchangeName;
  private String queueName;

  protected RabbitMQClient client;

  Vertx vertx;

  private static int getFreePort() {
    try (ServerSocket s = new ServerSocket(0)) {
      return s.getLocalPort();
    } catch(IOException ex) {
      return -1;
    }
  }

  @ClassRule
  public static final GenericContainer fixedRabbitmq = new FixedHostPortGenericContainer<>("rabbitmq:3.7-management")
    .withCreateContainerCmdModifier(cmd -> cmd.withHostName("bouncing-rabbit"))
    .withFixedExposedPort(getFreePort(), 5672);

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    exchangeName = this.getClass().getSimpleName() + "Exchange-" + UUID.randomUUID();
    queueName = this.getClass().getSimpleName() + "Queue-" + UUID.randomUUID();
  }

  public RabbitMQOptions config() throws Exception {
    RabbitMQOptions options = new RabbitMQOptions();
    options.setUri("amqp://" + fixedRabbitmq.getContainerIpAddress() + ":" + fixedRabbitmq.getMappedPort(5672));
    options.setAutomaticRecoveryEnabled(true);
    options.setReconnectAttempts(Integer.MAX_VALUE);
    options.setReconnectInterval(500);
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
    RabbitMQPublisher publisher = RabbitMQPublisher.create(vertx, client, new RabbitMQPublisherOptions()
      .setReconnectAttempts(Integer.MAX_VALUE)
      .setReconnectInterval(100)
      .setMaxInternalQueueSize(Integer.MAX_VALUE)
    );

    client.start().toCompletionStage().toCompletableFuture().get();
    publisher.start().toCompletionStage().toCompletableFuture().get();
    publisher.stop().toCompletionStage().toCompletableFuture().get();
  }

  @Test
  public void testPublishConfirm(TestContext ctx) throws Throwable {

    int count = 1000;

    Map<String, MessageDefinition> messages = new ConcurrentHashMap<>(count);
    for (int i = 0; i < count; ++i) {
      String messageId = String.format("NewID-%05d", i);
      messages.put(messageId, new MessageDefinition(i, messageId, "Message " + i));
    }
    Map<String, RabbitMQMessage> messagesReceived = new HashMap<>(count);

    Async receivedEnoughMessagesLatch = ctx.async(count);

    AtomicReference<RabbitMQConsumer> consumer = new AtomicReference<>();
    RabbitMQClient consumerClient = RabbitMQClient.create(vertx, config());
    AtomicLong duplicateCount = new AtomicLong();
    prepareClient(consumerClient,
      p -> consumerClient.queueDeclare(queueName, true, false, false)
        .compose(unused -> consumerClient.exchangeDeclare(exchangeName, "fanout", true, false))
        .compose(declareOk -> consumerClient.queueBind(queueName, exchangeName, ""))
        .onFailure(p::fail).onSuccess(p::complete),
      p -> consumerClient.basicConsumer(queueName, new QueueOptions().setAutoAck(false)).onComplete(ar4 -> {
        if (ar4.succeeded()) {
          consumer.set(ar4.result());
          ar4.result().handler(m -> {
            synchronized (messages) {
              Object prev = messagesReceived.put(m.properties().getMessageId(), m);
              if (prev != null) {
                duplicateCount.incrementAndGet();
              }
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
      })
    );

    this.client = RabbitMQClient.create(vertx, config());
    prepareClient(client,
      p -> client.queueDeclare(queueName, true, false, false)
      .compose(unused -> client.exchangeDeclare(exchangeName, "fanout", true, false))
      .compose(declareOk -> client.queueBind(queueName, exchangeName, ""))
      .onFailure(p::fail).onSuccess(p::complete),
      null);

    Async toConfirm = ctx.async(messages.size());

    RabbitMQPublisher publisher = RabbitMQPublisher.create(vertx, client, new RabbitMQPublisherOptions()
      .setReconnectAttempts(Integer.MAX_VALUE)
      .setReconnectInterval(100)
      .setMaxInternalQueueSize(Integer.MAX_VALUE));

    publisher.start().toCompletionStage().toCompletableFuture().get();

    for (MessageDefinition message : messages.values()) {
      Thread.sleep(1);
      publisher.publishConfirm(exchangeName, "", new AMQP.BasicProperties.Builder()
        .messageId(message.messageId).build(), Buffer.buffer(message.messageBody))
        .onComplete(event -> toConfirm.countDown());
    }
    logger.info("Still got " + publisher.queueSize() + " messages in the send queue, waiting for that to clear");


    logger.info("Waiting up to 20s for the latch");
    receivedEnoughMessagesLatch.await(20000L);
    toConfirm.await(20000L);
    logger.info("Latched, shutting down");

    logger.info("Shutting down");
    consumer.get().cancel()
        .compose(unused -> consumerClient.exchangeDelete(exchangeName))
          .compose(unused -> consumerClient.queueDelete(queueName))
            .compose(deleteOk -> consumerClient.stop())
              .onComplete(event -> ctx.async().complete());
  }

  @Ignore
  @Test
  public void testPublishOverReconnect(TestContext ctx) throws Throwable {

    int count = 1000;

    Map<String, MessageDefinition> messages = new ConcurrentHashMap<>(count);
    for (int i = 0; i < count; ++i) {
      String messageId = String.format("NewID-%05d", i);
      messages.put(messageId, new MessageDefinition(i, messageId, "Message " + i));
    }
    Map<String, RabbitMQMessage> messagesReceived = new ConcurrentHashMap<>(count);

    Async receivedEnoughMessagesLatch = ctx.async(count);

    AtomicReference<RabbitMQConsumer> consumer = new AtomicReference<>();
    RabbitMQClient consumerClient = RabbitMQClient.create(vertx, config());
    AtomicLong duplicateCount = new AtomicLong();
    prepareClient(consumerClient,
      p -> consumerClient.queueDeclare(queueName, true, false, false)
        .compose(unused -> consumerClient.exchangeDeclare(exchangeName, "fanout", true, false))
        .compose(declareOk -> consumerClient.queueBind(queueName, exchangeName, ""))
        .onFailure(p::fail).onSuccess(p::complete)
        , p -> consumerClient.basicConsumer(queueName, new QueueOptions().setAutoAck(false)).onComplete(ar4 -> {
          if (ar4.succeeded()) {
            consumer.set(ar4.result());
            ar4.result().handler(m -> {
                Object prev = messagesReceived.put(m.properties().getMessageId(), m);
                if (prev != null) {
                  duplicateCount.incrementAndGet();
                }
                consumerClient.basicAck(m.envelope().getDeliveryTag(), false);
                if (messagesReceived.size() == count) {
                  logger.info("Have received " + messagesReceived.size() + " messages with " + duplicateCount.get() + " duplicates");
                  receivedEnoughMessagesLatch.complete();
                }
            });
            p.complete(null);
          } else {
            p.completeExceptionally(ar4.cause());
          }
        })
    );


    this.client = RabbitMQClient.create(vertx, config());
    prepareClient(client,
      p -> client.queueDeclare(queueName, true, false, false)
        .compose(unused -> client.exchangeDeclare(exchangeName, "fanout", true, false))
        .compose(declareOk -> client.queueBind(queueName, exchangeName, ""))
        .onFailure(p::fail).onSuccess(p::complete),
      null);

    vertx.setTimer(100, l -> {
      vertx.executeBlocking(() -> {
        logger.info("Stopping rabbitmq container");
        fixedRabbitmq.stop();
        logger.info("Starting rabbitmq container");
        fixedRabbitmq.start();
        logger.info("Started rabbitmq container");
        return null;
      });
    });

    RabbitMQPublisher publisher = RabbitMQPublisher.create(vertx, client, new RabbitMQPublisherOptions()
      .setReconnectAttempts(Integer.MAX_VALUE)
      .setReconnectInterval(100)
      .setMaxInternalQueueSize(Integer.MAX_VALUE));
    publisher.start().toCompletionStage().toCompletableFuture().get();

    Async toConfirm = ctx.async(messages.size());

    List<MessageDefinition> messagesCopy;
    synchronized(messages) {
      messagesCopy = new ArrayList<>(messages.values());
      messagesCopy.sort(Comparator.comparing(l -> l.messageId));
    }
    for (MessageDefinition message : messagesCopy) {
      Thread.sleep(1);
      publisher.publishConfirm(exchangeName, "", new AMQP.BasicProperties.Builder()
          .messageId(message.messageId).build(), Buffer.buffer(message.messageBody))
        .onSuccess(event -> {
          messages.remove(message.messageId);
          toConfirm.countDown();
        });
    }
    logger.info("Still got " + publisher.queueSize() + " messages in the send queue, waiting for that to clear");
    publisher.stop().toCompletionStage().toCompletableFuture().get();
    publisher.restart();

    synchronized(messages) {
      logger.info("After the publisher has sent everything there remain " + messages.size() + " messages unconfirmed");
      messagesCopy = new ArrayList<>(messages.values());
      messagesCopy.sort(Comparator.comparing(l -> l.messageId));
    }


    for (MessageDefinition message : messagesCopy) {
      Thread.sleep(1);
      publisher.publishConfirm(exchangeName, "", new AMQP.BasicProperties.Builder()
            .messageId(message.messageId).build(), Buffer.buffer(message.messageBody))
        .onSuccess(event -> {
          messages.remove(message.messageId);
          toConfirm.countDown();
        });
    }

    logger.info("Waiting up to 20s for the latch");
    toConfirm.await(20000L);
    receivedEnoughMessagesLatch.await(20000L);
    logger.info("Latched, shutting down");

    logger.info("Shutting down");
    consumer.get().cancel().onComplete(ar1 -> {
      logger.info("Consumer cancelled");
      consumerClient.stop().onComplete(ar2 -> {
        logger.info("Consumer client stopped");
        client.stop().onComplete(ar3 -> {
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
    client.start().onComplete(ar -> {
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
