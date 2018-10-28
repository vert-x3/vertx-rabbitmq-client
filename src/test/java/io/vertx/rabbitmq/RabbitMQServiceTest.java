package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.vertx.test.core.TestUtils.randomAlphaString;
import static io.vertx.test.core.TestUtils.randomInt;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQServiceTest extends RabbitMQClientTestBase {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQServiceTest.class);

  @Override
  public void setUp() throws Exception {
    super.setUp();
    connect();
  }

  @Test
  public void testMessageOrderingWhenConsuming(TestContext ctx) throws IOException {

    String queueName = "message_ordering_test";
    String address = queueName + ".address";

    int count = 1000;

    List<String> sendingOrder = IntStream.range(1, count).boxed().map(Object::toString).collect(Collectors.toList());

    // set up queue
    AMQP.Queue.DeclareOk ok = channel.queueDeclare(queueName, false, false, true, null);
    ctx.assertNotNull(ok.getQueue());
    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").contentEncoding("UTF-8").build();

    // send  messages
    for (String msg : sendingOrder)
      channel.basicPublish("", queueName, properties, msg.getBytes("UTF-8"));

    List<String> receivedOrder = Collections.synchronizedList(new ArrayList<>());

    Async async = ctx.async(sendingOrder.size());
    vertx.eventBus().consumer(address, msg -> {
      ctx.assertNotNull(msg);
      JsonObject json = (JsonObject) msg.body();
      ctx.assertNotNull(json);
      String body = json.getString("body");
      ctx.assertNotNull(body);
      receivedOrder.add(body);
      async.countDown();
    });

    client.basicConsume(queueName, address, ctx.asyncAssertSuccess());

    async.awaitSuccess(15000);

    for (int i = 0; i < sendingOrder.size(); i++) {
      ctx.assertTrue(sendingOrder.get(i).equals(receivedOrder.get(i)));
    }
  }

  @Test
  public void testMessageOrderingWhenConsumingNewApi(TestContext ctx) throws IOException {

    String queueName = randomAlphaString(10);
    String address = queueName + ".address";

    int count = 1000;

    List<String> sendingOrder = IntStream.range(1, count).boxed().map(Object::toString).collect(Collectors.toList());

    // set up queue
    AMQP.Queue.DeclareOk ok = channel.queueDeclare(queueName, false, false, true, null);
    ctx.assertNotNull(ok.getQueue());
    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").contentEncoding("UTF-8").build();

    // send  messages
    for (String msg : sendingOrder)
      channel.basicPublish("", queueName, properties, msg.getBytes("UTF-8"));

    List<String> receivedOrder = Collections.synchronizedList(new ArrayList<>());

    Async async = ctx.async(sendingOrder.size());
    client.basicConsumer(queueName, consumerHandler -> {
      if (consumerHandler.succeeded()) {
        consumerHandler.result().handler(msg -> {
          ctx.assertNotNull(msg);
          String body = msg.body().toString();
          ctx.assertNotNull(body);
          receivedOrder.add(body);
          async.countDown();
        });
      } else {
        ctx.fail();
      }
    });

    async.awaitSuccess(15000);

    for (int i = 0; i < sendingOrder.size(); i++) {
      ctx.assertTrue(sendingOrder.get(i).equals(receivedOrder.get(i)));
    }
  }

  @Test
  public void testBasicGet(TestContext ctx) throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);
    Async async = ctx.async(count);

    // we have only ten seconds to get the 3 messages
    long timeOutFailTimer = vertx.setTimer(10_000, t -> ctx.fail());

    vertx.setPeriodic(100, id -> {
      client.basicGet(q, true, ctx.asyncAssertSuccess(msg -> {
        if (msg != null) {
          String body = msg.getString("body");
          ctx.assertTrue(messages.contains(body));
          async.countDown();
          if (async.count() == 0) {
            vertx.cancelTimer(id);
            vertx.cancelTimer(timeOutFailTimer);
          }
        }
      }));
    });
  }

  @Test
  public void testBasicPublish(TestContext ctx) throws Exception {
    String q = setupQueue(ctx, null);
    String body = randomAlphaString(100);
    JsonObject message = new JsonObject().put("body", body);
    client.basicPublish("", q, message, ctx.asyncAssertSuccess(v -> {
      client.basicGet(q, true, ctx.asyncAssertSuccess(msg -> {
        ctx.assertNotNull(msg);
        ctx.assertEquals(body, msg.getString("body"));
      }));
    }));
  }

  @Test
  public void testBasicPublishWithConfirm(TestContext ctx) throws Exception {
    String q = setupQueue(ctx, null);
    String body = randomAlphaString(100);
    JsonObject message = new JsonObject().put("body", body);

    client.confirmSelect(ctx.asyncAssertSuccess(v -> {
      client.basicPublish("", q, message, ctx.asyncAssertSuccess(vv -> {
        client.waitForConfirms(ctx.asyncAssertSuccess(vvv -> {
          client.basicGet(q, true, ctx.asyncAssertSuccess(msg -> {
            ctx.assertNotNull(msg);
            ctx.assertEquals(body, msg.getString("body"));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testBasicPublishWithConfirmAndTimeout(TestContext ctx) throws Exception {
    String q = setupQueue(ctx, null);
    String body = randomAlphaString(100);
    JsonObject message = new JsonObject().put("body", body);

    client.confirmSelect(ctx.asyncAssertSuccess(v -> {
      client.basicPublish("", q, message, ctx.asyncAssertSuccess(vv -> {
        client.waitForConfirms(1000, ctx.asyncAssertSuccess(vvv -> {
          client.basicGet(q, true, ctx.asyncAssertSuccess(msg -> {
            ctx.assertNotNull(msg);
            ctx.assertEquals(body, msg.getString("body"));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testBasicPublishJson(TestContext ctx) throws Exception {
    String q = setupQueue(ctx, null);
    JsonObject body = new JsonObject().put("foo", randomAlphaString(5)).put("bar", randomInt());
    JsonObject message = new JsonObject().put("body", body);
    message.put("properties", new JsonObject().put("contentType", "application/json"));
    client.basicPublish("", q, message, ctx.asyncAssertSuccess(v -> {
      client.basicGet(q, true, ctx.asyncAssertSuccess(msg -> {
        ctx.assertNotNull(msg);
        JsonObject b = msg.getJsonObject("body");
        ctx.assertNotNull(b);
        ctx.assertFalse(body == b);
        ctx.assertEquals(body, b);
      }));
    }));
  }

  @Test
  public void testBasicConsume(TestContext ctx) throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async latch = ctx.async(count);

    vertx.eventBus().consumer("my.address", msg -> {
      JsonObject json = (JsonObject) msg.body();
      ctx.assertNotNull(json);
      String body = json.getString("body");
      ctx.assertNotNull(body);
      ctx.assertTrue(messages.contains(body));
      latch.countDown();
    });

    client.basicConsume(q, "my.address", ctx.asyncAssertSuccess(v -> {
    }));
  }


  @Test
  public void testBasicCancel(TestContext ctx) throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async async = ctx.async();
    AtomicInteger received = new AtomicInteger(0);

    vertx.eventBus().consumer("my.address", msg -> {
      int receivedTotal = received.incrementAndGet();
      log.info(String.format("received %d-th message", receivedTotal));
      ctx.assertFalse(receivedTotal > count);
      if (receivedTotal == 3) {
        vertx.setTimer(1000, id -> {
          async.complete();
        });
      }
    });

    client.basicConsume(q, "my.address", ctx.asyncAssertSuccess(tag -> {
      client.basicCancel(tag);
      String body = randomAlphaString(100);
      JsonObject message = new JsonObject().put("body", body);
      client.basicPublish("", q, message, null);
    }));
  }

  @Test
  public void testBasicConsumer(TestContext ctx) throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async latch = ctx.async(count);

    client.basicConsumer(q, ctx.asyncAssertSuccess(consumer -> {
      System.out.println("CONSUMING");
      consumer.handler(msg -> {
        System.out.println("GOT MESSAGE");
        ctx.assertNotNull(msg);
        String body = msg.body().toString();
        ctx.assertNotNull(body);
        ctx.assertTrue(messages.contains(body));
        latch.countDown();
      });
    }));
  }

  @Test
  public void testBasicConsumeWithErrorHandler(TestContext ctx) throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages, "application/json");

    Async latch = ctx.async(count);

    vertx.eventBus().consumer("my.address", msg -> ctx.fail("Getting message with malformed json"));

    Handler<Throwable> errorHandler = throwable -> latch.countDown();

    client.basicConsume(q, "my.address", true, ctx.asyncAssertSuccess(), errorHandler);
  }

  @Test
  public void testBasicConsumerWithErrorHandler(TestContext ctx) throws Exception {
    int count = 1;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages, "application/json");

    Async latch = ctx.async(count);

    Handler<Throwable> errorHandler = throwable -> latch.countDown();

    client.basicConsumer(q, ctx.asyncAssertSuccess(consumer -> {
      consumer.exceptionHandler(errorHandler);
      consumer.handler(json -> {
        throw new IllegalStateException("Getting message with malformed json");
      });
    }));
  }

  @Test
  public void testBasicConsumeNoAutoAck(TestContext ctx) throws Exception {

    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async latch = ctx.async(count);

    vertx.eventBus().consumer("my.address", msg -> {
      JsonObject json = (JsonObject) msg.body();
      handleUnAckDelivery(ctx, messages, latch, json);
    });

    client.basicConsume(q, "my.address", false, ctx.asyncAssertSuccess(v -> {
    }));

    //assert all messages should be consumed.
    latch.awaitSuccess(15000);
    ctx.assertTrue(messages.isEmpty());
  }

  @Test
  public void testBasicConsumerNoAutoAck(TestContext ctx) throws Exception {

    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async latch = ctx.async(count);

    client.basicConsumer(q, new QueueOptions().setAutoAck(false), consumerHandler -> {
      if (consumerHandler.succeeded()) {
        log.info("Consumer started successfully");
        RabbitMQConsumer result = consumerHandler.result();
        result.exceptionHandler(e -> {
          log.error(e);
          ctx.fail();
        });
        result.handler(msg -> handleUnAckDelivery(ctx, messages, latch, msg));
      } else {
        ctx.fail();
      }
    });

    latch.awaitSuccess(15000);
    //assert all messages should be consumed.
    ctx.assertTrue(messages.isEmpty());
  }

  private void handleUnAckDelivery(TestContext ctx, Set<String> messages, Async latch, JsonObject json) {
    String body = json.getString("body");
    ctx.assertTrue(messages.contains(body));
    Long deliveryTag = json.getLong("deliveryTag");
    if (json.getBoolean("isRedeliver")) {
      client.basicAck(deliveryTag, false, ctx.asyncAssertSuccess(v -> {
        // remove the message if is redeliver (unacked)
        messages.remove(body);
        latch.countDown();
      }));
    } else {
      // send and Nack for every ready message
      client.basicNack(deliveryTag, false, true, ctx.asyncAssertSuccess());
    }
  }

  private void handleUnAckDelivery(TestContext ctx, Set<String> messages, Async async, RabbitMQMessage message) {
    String body = message.body().toString();
    ctx.assertTrue(messages.contains(body));
    Long deliveryTag = message.envelope().deliveryTag();
    log.info("message arrived: " + message.body().toString(message.properties().contentEncoding()));
    log.info("redelivered? : " + message.envelope().isRedelivery());
    if (message.envelope().isRedelivery()) {
      client.basicAck(deliveryTag, false, ctx.asyncAssertSuccess(v -> {
        // remove the message if is redeliver (unacked)
        messages.remove(body);
        async.countDown();
      }));
    } else {
      // send and Nack for every ready message
      client.basicNack(deliveryTag, false, true, ctx.asyncAssertSuccess());
    }
  }

  @Test
  public void testQueueDeclareAndDelete(TestContext ctx) {
    String queueName = randomAlphaString(10);

    client.queueDeclare(queueName, false, false, true, ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(result.getString("queue"), queueName);

      client.queueDelete(queueName, ctx.asyncAssertSuccess());
    }));
  }

  @Test
  public void testQueueDeclareAndDeleteWithConfig(TestContext ctx) {
    String queueName = randomAlphaString(10);
    JsonObject config = new JsonObject();
    config.put("x-message-ttl", 10_000L);

    client.queueDeclare(queueName, false, false, true, config, ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(result.getString("queue"), queueName);

      client.queueDelete(queueName, ctx.asyncAssertSuccess());
    }));
  }

  //TODO: create an integration test with a test scenario
  @Test
  public void testDeclareExchangeWithAlternateExchange(TestContext ctx) throws Exception {
    String exName = randomAlphaString(10);
    Map<String, String> params = new HashMap<>();
    params.put("alternate-exchange", "alt.ex");
    client.exchangeDeclare(exName, "direct", false, true, params, ctx.asyncAssertSuccess());

  }

  //TODO: create an integration test with a test scenario
  @Test
  public void testDeclareExchangeWithDLX(TestContext ctx) throws Exception {
    String exName = randomAlphaString(10);
    Map<String, String> params = new HashMap<>();
    params.put("x-dead-letter-exchange", "dlx.exchange");
    client.exchangeDeclare(exName, "direct", false, true, params, ctx.asyncAssertSuccess());
  }

  @Test
  public void testIsOpenChannel(TestContext ctx) {

    boolean result = client.isOpenChannel();

    ctx.assertTrue(result);

    client.stop(ctx.asyncAssertSuccess(v -> {
      ctx.assertFalse(client.isOpenChannel());
    }));
  }

  @Test
  public void testIsConnected(TestContext ctx) {

    boolean result = client.isConnected();

    ctx.assertTrue(result);

    client.stop(ctx.asyncAssertSuccess(v -> {
      ctx.assertFalse(client.isConnected());
    }));
  }

  @Test
  public void testGetMessageCount(TestContext ctx) throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String queue = setupQueue(ctx, messages);
    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.messageCount(queue, ctx.asyncAssertSuccess(messageCount -> {
          ctx.assertEquals(count, messageCount.intValue());

          // remove the queue
          client.queueDelete(queue, ctx.asyncAssertSuccess(json -> async.complete()));
        })
      )
    );
  }

  @Test
  public void consumerPrefetch(TestContext ctx) throws Exception {
    // 1. Limit number of unack messages to 2
    // 2. Send 3 messages
    // 3. Ensure only 2 messages received
    int count = 3;
    int amountOfUnAckMessages = count - 1;

    Async prefetchDone = ctx.async();
    client.basicQos(amountOfUnAckMessages, ctx.asyncAssertSuccess(v -> prefetchDone.complete()));
    prefetchDone.await();

    Set<String> messages = createMessages(count);
    String queue = setupQueue(ctx, messages);
    String address = queue + ".address";

    Async receivedExpectedNumberOfMessages = ctx.async(amountOfUnAckMessages);

    vertx.eventBus().consumer(address, msg -> {
      ctx.assertFalse(receivedExpectedNumberOfMessages.isCompleted());
      receivedExpectedNumberOfMessages.countDown();
    });

    client.basicConsume(queue, address, false, ctx.asyncAssertSuccess());

    receivedExpectedNumberOfMessages.awaitSuccess(15000);

    // At the point we are sure, that we have already received 2 messages.
    // But, if 3rd message will arrive the test will fail in the next second.
    Async async = ctx.async();
    vertx.setTimer(1000, spent -> async.countDown());
  }

  //TODO More tests
}
