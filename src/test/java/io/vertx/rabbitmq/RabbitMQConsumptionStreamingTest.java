package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static io.vertx.test.core.TestUtils.randomAlphaString;

public class RabbitMQConsumptionStreamingTest extends RabbitMQClientTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    connect();
  }

  @Test
  public void consumerTagShouldBeTheSameAsInAMessage() throws Exception {
    int count = 1;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    CountDownLatch messagesReceived = new CountDownLatch(count);

    client.basicConsumer(q, consumerHandler -> {
      if (consumerHandler.succeeded()) {
        consumerHandler.result().handler(json -> {
          assertNotNull(json);
          String tag = json.getString("consumerTag");
          assertTrue(tag.equals(consumerHandler.result().consumerTag()));
          String body = json.getString("body");
          assertNotNull(body);
          assertTrue(messages.contains(body));
          messagesReceived.countDown();
        });
      } else {
        fail();
      }
    });

    awaitLatch(messagesReceived);
    testComplete();
  }


  @Test
  public void pauseAndResumeShouldWork() throws Exception {
    int count = 1;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    CountDownLatch paused = new CountDownLatch(1);
    CountDownLatch resumed = new CountDownLatch(1);
    CountDownLatch messageReceived = new CountDownLatch(1);

    client.basicConsumer(q, new QueueOptions().setBuffer(true), consumerHandler -> {
      if (consumerHandler.succeeded()) {
        RabbitMQConsumer mqConsumer = consumerHandler.result();
        mqConsumer.pause();
        paused.countDown();
        mqConsumer.handler(json -> {
          assertNotNull(json);
          // if not resumed, test should fail
          if (resumed.getCount() == 1) {
            fail();
          } else {
            messageReceived.countDown();
          }
        });

        // wait for resume command
        try {
          awaitLatch(resumed);
        } catch (InterruptedException e) {
          fail();
        }
        mqConsumer.resume();
      } else {
        fail();
      }
    });

    awaitLatch(paused);

    // wait some time to ensure that handler will not receive any messages when it is paused
    Thread.sleep(1000);

    // report to stream to be resumed
    resumed.countDown();

    // ensure message received after the stream resume
    awaitLatch(messageReceived);
    testComplete();
  }


  @Test
  public void endHandlerAndCancelShouldWorks() throws Exception {
    String q = "my.favorite.queue";

    channel.queueDeclare(q, false, false, true, null);

    CountDownLatch canceled = new CountDownLatch(1);
    CountDownLatch endOfStream = new CountDownLatch(1);

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

        mqConsumer.handler(json -> fail());
      } else {
        fail();
      }
    });

    awaitLatch(canceled);
    awaitLatch(endOfStream);

    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().build();
    channel.basicPublish("", q, properties, "whatever".getBytes());

    // wait some time to ensure that handler will not receive any messages when the stream is ended
    Thread.sleep(1000);

    testComplete();
  }

  @Test
  public void whenBufferIsDisabledClientShouldNotReceiveAnyMessages() throws Exception {
    String q =  randomAlphaString(10);

    channel.queueDeclare(q, false, false, true, null);

    CountDownLatch paused = new CountDownLatch(1);
    CountDownLatch resumed = new CountDownLatch(1);

    client.basicConsumer(q, new QueueOptions().setBuffer(false), consumerHandler -> {
      if (consumerHandler.succeeded()) {
        RabbitMQConsumer mqConsumer = consumerHandler.result();
        mqConsumer.handler(json -> fail());
        mqConsumer.pause();
        paused.countDown();
        try {
          awaitLatch(resumed);
        } catch (InterruptedException e) {
          fail();
        }
        mqConsumer.resume();
      } else {
        fail();
      }
    });

    awaitLatch(paused);

    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().build();
    channel.basicPublish("", q, properties, "whatever".getBytes());

    resumed.countDown();

    // wait some time to ensure that handler will not receive any messages
    // since when it was paused messages were not buffered
    Thread.sleep(1000);

    testComplete();
  }

}
