package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.test.core.TestUtils.randomAlphaString;

/**
 * Testing of rabbitmq client consumption streaming capabilities
 */
@RunWith(VertxUnitRunner.class)
public class RabbitMQConsumptionStreamingTest extends RabbitMQClientTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    connect();
  }

  @Test
  public void consumerTagShouldBeTheSameAsInAMessage(TestContext context) throws Exception {
    int count = 1;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    Async messagesReceived = context.async(count);

    client.basicConsumer(q, consumerHandler -> {
      if (consumerHandler.succeeded()) {
        consumerHandler.result().handler(msg -> {
          assertNotNull(msg);
          String tag = msg.consumerTag();
          assertTrue(tag.equals(consumerHandler.result().consumerTag()));
          String body = msg.body().toString();
          assertNotNull(body);
          assertTrue(messages.contains(body));
          messagesReceived.countDown();
        });
      } else {
        context.fail();
      }
    });
  }


  @Test
  public void pauseAndResumeShouldWork(TestContext context) throws Exception {
    int count = 1;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    Async paused = context.async();
    Async resumed = context.async();
    Async messageReceived = context.async();

    client.basicConsumer(q, new QueueOptions().setBuffer(true), consumerHandler -> {
      if (consumerHandler.succeeded()) {
        RabbitMQConsumer mqConsumer = consumerHandler.result();
        mqConsumer.pause();
        mqConsumer.handler(msg -> {
          assertNotNull(msg);
          // if not resumed, test should fail
          if (resumed.count() == 1) {
            context.fail();
          } else {
            messageReceived.countDown();
          }
        });
        paused.countDown();
        // wait for resume command
        resumed.await();
        mqConsumer.resume();
      } else {
        context.fail();
      }
    });

    paused.await();

    // wait some time to ensure that handler will not receive any messages when it is paused
    vertx.setTimer(1000, t -> resumed.countDown());
  }


  @Test
  public void endHandlerAndCancelShouldWorks(TestContext context) throws Exception {
    String q = randomAlphaString(10);

    channel.queueDeclare(q, false, false, true, null);

    Async canceled = context.async();
    Async endOfStream = context.async();

    client.basicConsumer(q, consumerHandler -> {
      if (consumerHandler.succeeded()) {
        RabbitMQConsumer mqConsumer = consumerHandler.result();
        mqConsumer.endHandler(v -> endOfStream.countDown());
        mqConsumer.cancel(v -> {
          if (v.succeeded()) {
            canceled.countDown();
          } else {
            fail();
          }
        });

        mqConsumer.handler(msg -> fail());
      } else {
        fail();
      }
    });

    canceled.await();
    endOfStream.await();

    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().build();
    channel.basicPublish("", q, properties, "whatever".getBytes());

    // wait some time to ensure that handler will not receive any messages when the stream is ended
    Async done = context.async();
    vertx.setTimer(1000, l -> done.countDown());
  }

  @Test
  public void whenStreamIsPausedAndNoBufferingMessagesShouldNotBeStored(TestContext context) throws Exception {
    String q = randomAlphaString(10);

    channel.queueDeclare(q, false, false, true, null);

    Async paused = context.async();
    AtomicReference<RabbitMQConsumer> mqConsumer = new AtomicReference<>(null);
    client.basicConsumer(q, new QueueOptions().setBuffer(false), consumerHandler -> {
      if (consumerHandler.succeeded()) {
        RabbitMQConsumer consumer = consumerHandler.result();
        mqConsumer.set(consumer);
        consumer.handler(msg -> context.fail());
        consumer.pause();
        paused.countDown();
      } else {
        context.fail();
      }
    });

    paused.await();
    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().build();
    channel.basicPublish("", q, properties, "whatever".getBytes());

    Async done = context.async();

    // resume in 10 seconds, so message should be received, but not stored
    vertx.setTimer(1000, l -> {
      mqConsumer.get().resume();
      // wait some time to ensure that handler will not receive any messages
      // since when it was paused messages were not buffered
      vertx.setTimer(1000, t -> done.countDown());
    });
  }

  @Test
  public void keepMostRecentOptionShouldWorks(TestContext context) throws Exception {
    int count = 2;
    int queueSize = 1;
    Set<String> messages = createMessages(count);
    Iterator<String> iterator = messages.iterator();

    iterator.next();
    String secondMessage = iterator.next();

    String q = setupQueue(messages);

    Async paused = context.async();
    AtomicReference<RabbitMQConsumer> mqConsumer = new AtomicReference<>(null);
    QueueOptions queueOptions = new QueueOptions().setKeepMostRecent(true).setMaxInternalQueueSize(queueSize);

    client.basicConsumer(q, queueOptions, consumerHandler -> {
      if (consumerHandler.succeeded()) {
        RabbitMQConsumer consumer = consumerHandler.result();
        mqConsumer.set(consumer);
        consumer.pause();
        consumer.handler(msg -> {
          assertTrue("only second message should be stored", msg.body().toString().equals(secondMessage));
        });
        paused.countDown();
      } else {
        fail();
      }
    });

    paused.await();

    Async done = context.async();
    // resume in 10 seconds, so message should be received and stored
    vertx.setTimer(1000, l -> {
      mqConsumer.get().resume();
      // wait some time to ensure that handler will be called only with the second message
      vertx.setTimer(1000, t -> done.countDown());
    });
  }
}
