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
  public void consumerTagShouldBeTheSameAsInAMessage(TestContext ctx) throws Exception {
    int count = 1;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async messagesReceived = ctx.async(count);

    client.basicConsumer(q).onComplete(ctx.asyncAssertSuccess(consumer -> {
      consumer.handler(msg -> {
        ctx.assertNotNull(msg);
        String tag = msg.consumerTag();
        ctx.assertTrue(tag.equals(consumer.consumerTag()));
        String body = msg.body().toString();
        ctx.assertNotNull(body);
        ctx.assertTrue(messages.contains(body));
        messagesReceived.countDown();
      });
    }));
  }


  @Test
  public void pauseAndResumeShouldWork(TestContext ctx) throws Exception {
    int count = 1;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async paused = ctx.async();
    Async resumed = ctx.async();
    Async messageReceived = ctx.async();

    client.basicConsumer(q, new QueueOptions()).onComplete(ctx.asyncAssertSuccess(consumer -> {
      consumer.pause();
      consumer.handler(msg -> {
        ctx.assertNotNull(msg);
        // if not resumed, test should fail
        if (resumed.count() == 1) {
          ctx.fail();
        } else {
          messageReceived.complete();
        }
      });
      paused.complete();
      // wait for resume command
      resumed.await();
      consumer.resume();
    }));

    paused.awaitSuccess(15000);

    // wait some time to ensure that handler will not receive any messages when it is paused
    Thread.sleep(1000);
    resumed.complete();
  }


  @Test
  public void endHandlerAndCancelShouldWork(TestContext ctx) throws Exception {
    String q = randomAlphaString(10);

    channel.queueDeclare(q, false, false, true, null);

    Async canceled = ctx.async();
    Async endOfStream = ctx.async();

    client.basicConsumer(q).onComplete(ctx.asyncAssertSuccess(consumer -> {
      consumer.endHandler(v -> endOfStream.complete());
      consumer.handler(msg -> ctx.fail());
      vertx.executeBlocking(() -> {
        consumer.cancel().onComplete(ctx.asyncAssertSuccess(v -> canceled.complete()));
        return null;
      });
    }));

    canceled.awaitSuccess(15000);
    endOfStream.awaitSuccess(15000);

    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().build();
    channel.basicPublish("", q, properties, "whatever".getBytes());

    // wait some time to ensure that handler will not receive any messages when the stream is ended
    Async done = ctx.async();
    vertx.setTimer(1000, l -> done.complete());
  }

  @Test
  public void keepMostRecentOptionShouldWorks(TestContext ctx) throws Exception {
    int count = 2;
    int queueSize = 1;
    Set<String> messages = createMessages(count);
    Iterator<String> iterator = messages.iterator();

    iterator.next();
    String secondMessage = iterator.next();

    String q = setupQueue(ctx, messages);

    Async paused = ctx.async();
    Async secondReceived = ctx.async();
    AtomicReference<RabbitMQConsumer> mqConsumer = new AtomicReference<>(null);
    QueueOptions queueOptions = new QueueOptions()
      .setKeepMostRecent(true)
      .setMaxInternalQueueSize(queueSize);

    client.basicConsumer(q, queueOptions).onComplete(ctx.asyncAssertSuccess(consumer -> {
      mqConsumer.set(consumer);
      consumer.pause();
      consumer.handler(msg -> {
        ctx.assertTrue(msg.body().toString().equals(secondMessage), "only second message should be stored");
        secondReceived.complete();
      });
      paused.complete();
    }));

    paused.awaitSuccess(15000);

    Async done = ctx.async();
    // resume in 10 seconds, so message should be received and stored
    vertx.setTimer(1000, l -> {
      mqConsumer.get().resume();
      // wait some time to ensure that handler will be called only with the second message
      vertx.setTimer(1000, t -> done.complete());
    });
  }
}
