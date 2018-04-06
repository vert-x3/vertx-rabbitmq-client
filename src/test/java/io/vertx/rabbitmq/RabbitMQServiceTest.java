package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.vertx.test.core.TestUtils.randomAlphaString;
import static io.vertx.test.core.TestUtils.randomInt;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@RunWith(VertxUnitRunner.class)
public class RabbitMQServiceTest extends RabbitMQClientTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    connect();
  }

  @Test
  public void testMessageOrderingWhenConsuming() throws IOException {

    String queueName = "message_ordering_test";
    String address = queueName + ".address";

    int count = 1000;

    List<String> sendingOrder = IntStream.range(1, count).boxed().map(Object::toString).collect(Collectors.toList());

    // set up queue
    AMQP.Queue.DeclareOk ok = channel.queueDeclare(queueName, false, false, true, null);
    assertNotNull(ok.getQueue());
    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").contentEncoding("UTF-8").build();

    // send  messages
    for (String msg : sendingOrder)
      channel.basicPublish("", queueName, properties, msg.getBytes("UTF-8"));

    List<String> receivedOrder = Collections.synchronizedList(new ArrayList<>());

    vertx.eventBus().consumer(address, msg -> {
      assertNotNull(msg);
      JsonObject json = (JsonObject) msg.body();
      assertNotNull(json);
      String body = json.getString("body");
      assertNotNull(body);
      receivedOrder.add(body);
    });

    client.basicConsume(queueName, address, onSuccess(v -> {
    }));


    assertWaitUntil(() -> receivedOrder.size() == sendingOrder.size());
    for (int i = 0; i < sendingOrder.size(); i++) {
      assertTrue(sendingOrder.get(i).equals(receivedOrder.get(i)));
    }
  }


  @Test
  public void testMessageOrderingWhenConsumingNewApi() throws IOException {

    String queueName =  randomAlphaString(10);
    String address = queueName + ".address";

    int count = 1000;

    List<String> sendingOrder = IntStream.range(1, count).boxed().map(Object::toString).collect(Collectors.toList());

    // set up queue
    AMQP.Queue.DeclareOk ok = channel.queueDeclare(queueName, false, false, true, null);
    assertNotNull(ok.getQueue());
    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("text/plain").contentEncoding("UTF-8").build();

    // send  messages
    for (String msg : sendingOrder)
      channel.basicPublish("", queueName, properties, msg.getBytes("UTF-8"));

    List<String> receivedOrder = Collections.synchronizedList(new ArrayList<>());

    client.basicConsumer(queueName, consumerHandler -> {
      if (consumerHandler.succeeded()) {
        consumerHandler.result().handler(msg -> {
          assertNotNull(msg);
          String body = msg.body().toString();
          assertNotNull(body);
          receivedOrder.add(body);
        });
      } else {
        fail();
      }
    });


    assertWaitUntil(() -> receivedOrder.size() == sendingOrder.size());
    for (int i = 0; i < sendingOrder.size(); i++) {
      assertTrue(sendingOrder.get(i).equals(receivedOrder.get(i)));
    }
  }

  @Test
  public void testBasicGet() throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);
    CountDownLatch latch = new CountDownLatch(count);
    for (int i = 0; i < count; i++) {
      client.basicGet(q, true, onSuccess(msg -> {
        assertNotNull(msg);
        String body = msg.getString("body");
        assertTrue(messages.contains(body));
        latch.countDown();
      }));
    }

    awaitLatch(latch);
    testComplete();
  }

  @Test
  public void testBasicPublish() throws Exception {
    String q = setupQueue(null);
    String body = randomAlphaString(100);
    JsonObject message = new JsonObject().put("body", body);
    client.basicPublish("", q, message, onSuccess(v -> {
      client.basicGet(q, true, onSuccess(msg -> {
        assertNotNull(msg);
        assertEquals(body, msg.getString("body"));
        testComplete();
      }));
    }));

    await();
  }

  @Test
  public void testBasicPublishWithConfirm() throws Exception {
    String q = setupQueue(null);
    String body = randomAlphaString(100);
    JsonObject message = new JsonObject().put("body", body);

    client.confirmSelect(onSuccess(v -> {
      client.basicPublish("", q, message, onSuccess(vv -> {
        client.waitForConfirms(onSuccess(vvv -> {
          client.basicGet(q, true, onSuccess(msg -> {
            assertNotNull(msg);
            assertEquals(body, msg.getString("body"));
            testComplete();
          }));
        }));
      }));
    }));

    await();
  }

  @Test
  public void testBasicPublishWithConfirmAndTimeout() throws Exception {
    String q = setupQueue(null);
    String body = randomAlphaString(100);
    JsonObject message = new JsonObject().put("body", body);

    client.confirmSelect(onSuccess(v -> {
      client.basicPublish("", q, message, onSuccess(vv -> {
        client.waitForConfirms(1000, onSuccess(vvv -> {
          client.basicGet(q, true, onSuccess(msg -> {
            assertNotNull(msg);
            assertEquals(body, msg.getString("body"));
            testComplete();
          }));
        }));
      }));
    }));

    await();
  }

  @Test
  public void testBasicPublishJson() throws Exception {
    String q = setupQueue(null);
    JsonObject body = new JsonObject().put("foo", randomAlphaString(5)).put("bar", randomInt());
    JsonObject message = new JsonObject().put("body", body);
    message.put("properties", new JsonObject().put("contentType", "application/json"));
    client.basicPublish("", q, message, onSuccess(v -> {
      client.basicGet(q, true, onSuccess(msg -> {
        assertNotNull(msg);
        JsonObject b = msg.getJsonObject("body");
        assertNotNull(b);
        assertFalse(body == b);
        assertEquals(body, b);
        testComplete();
      }));
    }));

    await();
  }

  @Test
  public void testBasicConsume() throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    CountDownLatch latch = new CountDownLatch(count);

    vertx.eventBus().consumer("my.address", msg -> {
      JsonObject json = (JsonObject) msg.body();
      assertNotNull(json);
      String body = json.getString("body");
      assertNotNull(body);
      assertTrue(messages.contains(body));
      latch.countDown();
    });

    client.basicConsume(q, "my.address", onSuccess(v -> {
    }));

    awaitLatch(latch);
    testComplete();
  }

  @Test
  public void testBasicConsumer() throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    CountDownLatch latch = new CountDownLatch(count);

    client.basicConsumer(q, consumerHandler -> {
      if (consumerHandler.succeeded()) {
        consumerHandler.result().handler(msg -> {
          assertNotNull(msg);
          String body = msg.body().toString();
          assertNotNull(body);
          assertTrue(messages.contains(body));
          latch.countDown();
        });
      } else {
        fail();
      }
    });

    awaitLatch(latch);
    testComplete();
  }

  @Test
  public void testBasicConsumeWithErrorHandler() throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages, "application/json");

    CountDownLatch latch = new CountDownLatch(count);

    vertx.eventBus().consumer("my.address", msg -> fail("Getting message with malformed json"));

    Handler<Throwable> errorHandler = throwable -> latch.countDown();

    client.basicConsume(q, "my.address", true, onSuccess(v -> {}), errorHandler);

    awaitLatch(latch);
    testComplete();
  }

  @Test
  public void testBasicConsumerWithErrorHandler(TestContext context) throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages, "application/json");

    Async latch = context.async(count);

    Handler<Throwable> errorHandler = throwable -> latch.countDown();

    client.basicConsumer(q, consumerHandler -> {
      if (consumerHandler.succeeded()) {
        RabbitMQConsumer result = consumerHandler.result();
        result.exceptionHandler(errorHandler);
        result.handler(json -> fail("Getting message with malformed json"));
      } else {
        fail();
      }
    });
  }

  @Test
  public void testBasicConsumeNoAutoAck() throws Exception {

    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    CountDownLatch latch = new CountDownLatch(count);

    vertx.eventBus().consumer("my.address", msg -> {
      JsonObject json = (JsonObject) msg.body();
      handleUnAckDelivery(messages, latch, json);
    });

    client.basicConsume(q, "my.address", false, onSuccess(v -> {
    }));

    awaitLatch(latch);
    //assert all messages should be consumed.
    assertTrue(messages.isEmpty());
    testComplete();
  }

  @Test
  public void testBasicConsumerNoAutoAck() throws Exception {

    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    CountDownLatch latch = new CountDownLatch(count);

    client.basicConsumer(q, new QueueOptions().setAutoAck(false), consumerHandler -> {
      if (consumerHandler.succeeded()) {
        RabbitMQConsumer result = consumerHandler.result();
        result.handler(msg -> handleUnAckDelivery(messages, latch, msg));
      } else {
        fail();
      }
    });


    awaitLatch(latch);
    //assert all messages should be consumed.
    assertTrue(messages.isEmpty());
    testComplete();
  }

  private void handleUnAckDelivery(Set<String> messages, CountDownLatch latch, JsonObject json) {
    String body = json.getString("body");
    assertTrue(messages.contains(body));
    Long deliveryTag = json.getLong("deliveryTag");
    if (json.getBoolean("isRedeliver")) {
      client.basicAck(deliveryTag, false, onSuccess(v -> {
        // remove the message if is redeliver (unacked)
        messages.remove(body);
        latch.countDown();
      }));
    } else {
      // send and Nack for every ready message
      client.basicNack(deliveryTag, false, true, onSuccess(v -> {
      }));
    }
  }

  private void handleUnAckDelivery(Set<String> messages, CountDownLatch latch, RabbitMQMessage message) {
    String body = message.body().toString();
    assertTrue(messages.contains(body));
    Long deliveryTag = message.envelope().deliveryTag();
    if (message.envelope().isRedeliver()) {
      client.basicAck(deliveryTag, false, onSuccess(v -> {
        // remove the message if is redeliver (unacked)
        messages.remove(body);
        latch.countDown();
      }));
    } else {
      // send and Nack for every ready message
      client.basicNack(deliveryTag, false, true, onSuccess(v -> {
      }));
    }
  }

  @Test
  public void testQueueDeclareAndDelete() {
    String queueName = randomAlphaString(10);

    client.queueDeclare(queueName, false, false, true, asyncResult -> {
      assertTrue(asyncResult.succeeded());
      JsonObject result = asyncResult.result();
      assertEquals(result.getString("queue"), queueName);

      client.queueDelete(queueName, deleteAsyncResult -> {
        assertTrue(deleteAsyncResult.succeeded());
        testComplete();
      });
    });

    await();
  }

  @Test
  public void testQueueDeclareAndDeleteWithConfig() {
    String queueName = randomAlphaString(10);
    JsonObject config = new JsonObject();
    config.put("x-message-ttl", 10_000L);

    client.queueDeclare(queueName, false, false, true, config, asyncResult -> {
      assertTrue(asyncResult.succeeded());
      JsonObject result = asyncResult.result();
      assertEquals(result.getString("queue"), queueName);

      client.queueDelete(queueName, deleteAsyncResult -> {
        assertTrue(deleteAsyncResult.succeeded());
        testComplete();
      });
    });

    await();
  }

  //TODO: create an integration test with a test scenario
  @Test
  public void testDeclareExchangeWithAlternateExchange() throws Exception {
    String exName = randomAlphaString(10);
    Map<String, String> params = new HashMap<>();
    params.put("alternate-exchange", "alt.ex");
    client.exchangeDeclare(exName, "direct", false, true, params, createResult -> {
      assertTrue(createResult.succeeded());
      testComplete();
    });

  }

  //TODO: create an integration test with a test scenario
  @Test
  public void testDeclareExchangeWithDLX() throws Exception {
    String exName = randomAlphaString(10);
    Map<String, String> params = new HashMap<>();
    params.put("x-dead-letter-exchange", "dlx.exchange");
    client.exchangeDeclare(exName, "direct", false, true, params, createResult -> {
      assertTrue(createResult.succeeded());
      testComplete();
    });
  }

  @Test
  public void testIsOpenChannel() {

    boolean result = client.isOpenChannel();

    assertTrue(result);

    client.stop(voidAsyncResult -> {
      assertFalse(client.isOpenChannel());
      testComplete();
    });

    await();
  }

  @Test
  public void testIsConnected() {

    boolean result = client.isConnected();

    assertTrue(result);

    client.stop(voidAsyncResult -> {
      assertFalse(client.isConnected());
      testComplete();
    });

    await();
  }

  @Test
  public void testGetMessageCount() throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);

    String queue = setupQueue(messages);

    client.messageCount(queue, onSuccess(json -> {
      long messageCount = json.getLong("messageCount");
      assertEquals(count, messageCount);

      // remove the queue
      client.queueDelete(queue, deleteAsyncResult -> {
        testComplete();
      });
    }));

    await();
  }

  @Test
  public void consumerPrefetch(TestContext context) throws Exception {
    // 1. Limit number of unack messages to 2
    // 2. Send 3 messages
    // 3. Ensure only 2 messages received
    int count = 3;
    int amountOfUnAckMessages = count - 1;

    Async prefetchDone = context.async();
    client.basicQos(amountOfUnAckMessages, done -> prefetchDone.countDown());
    prefetchDone.await();

    Set<String> messages = createMessages(count);
    String queue = setupQueue(messages);
    String address = queue + ".address";

    Async receivedExpectedNumberOfMessages = context.async(amountOfUnAckMessages);

    vertx.eventBus().consumer(address, msg -> {
      if (receivedExpectedNumberOfMessages.isCompleted()) {
        context.fail();
      } else {
        receivedExpectedNumberOfMessages.countDown();
      }
    });

    client.basicConsume(queue, address, false, onSuccess(v -> {
    }));

    receivedExpectedNumberOfMessages.await();

    // At the point we are sure, that we have already received 2 messages.
    // But, if 3rd message will arrive the test will fail in the next second.
    Async async = context.async();
    vertx.setTimer(1000, spent -> async.countDown());
  }

  //TODO More tests
}
