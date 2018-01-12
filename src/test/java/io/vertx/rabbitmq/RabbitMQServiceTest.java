package io.vertx.rabbitmq;

import static io.vertx.test.core.TestUtils.randomAlphaString;
import static io.vertx.test.core.TestUtils.randomInt;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.sniffy.socket.DisableSockets;
import io.sniffy.test.junit.SniffyRule;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Rule;
import org.junit.Test;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQServiceTest extends VertxTestBase {
  public static final String CLOUD_AMQP_URI = "amqps://xvjvsrrc:VbuL1atClKt7zVNQha0bnnScbNvGiqgb@moose.rmq.cloudamqp" +
    ".com/xvjvsrrc";
  protected RabbitMQClient client;

  private Channel channel;

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    if(channel != null) channel.close();
    super.tearDown();
  }

  public RabbitMQOptions config() {
    return new RabbitMQOptions();
  }

  @Rule
  public SniffyRule sniffyRule = new SniffyRule();

  @Test
  @DisableSockets
  public void testStartWithReconnectFuture() throws Exception {
    Future<Void> future = Future.future();
    CountDownLatch latch = new CountDownLatch(1);
    RabbitMQOptions options = "true".equalsIgnoreCase(System.getProperty("rabbitmq.local")) ?  config() : config().setUri(CLOUD_AMQP_URI);
    options.setConnectionRetries(3);
    options.setConnectionRetryDelay(1L);
    RabbitMQClient.create(Vertx.vertx(), options).start(future.completer());
    future.setHandler(onFailure(v -> {
      latch.countDown();
    }));
    awaitLatch(latch);
    assertTrue(future.isComplete() && future.failed());
  }

  @Test
  public void testBasicGet() throws Exception {
    startClient();
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
    startClient();
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
    startClient();
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
    startClient();
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
    startClient();
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
    startClient();
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
  public void testBasicConsumeWithErrorHandler() throws Exception {
    startClient();
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
  public void testBasicConsumeNoAutoAck() throws Exception {
    startClient();
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(messages);

    CountDownLatch latch = new CountDownLatch(count);

    vertx.eventBus().consumer("my.address", msg -> {
      JsonObject json = (JsonObject) msg.body();
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

    });

    client.basicConsume(q, "my.address", false, onSuccess(v -> {
    }));

    awaitLatch(latch);
    //assert all messages should be consumed.
    assertTrue(messages.isEmpty());
    testComplete();
  }

  @Test
  public void testQueueDeclareAndDelete() throws Exception {
    startClient();
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

  //TODO: create an integration test with a test scenario
  @Test
  public void testDeclareExchangeWithAlternateExchange() throws Exception {
    startClient();
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
    startClient();
    String exName = randomAlphaString(10);
    Map<String, String> params = new HashMap<>();
    params.put("x-dead-letter-exchange", "dlx.exchange");
    client.exchangeDeclare(exName, "direct", false, true, params, createResult -> {
      assertTrue(createResult.succeeded());
      testComplete();
    });
  }

  @Test
  public void testIsOpenChannel() throws Exception {
    startClient();
    boolean result = client.isOpenChannel();

    assertTrue(result);

    client.stop(voidAsyncResult -> {
      assertFalse(client.isOpenChannel());
      testComplete();
    });

    await();
  }

  @Test
  public void testIsConnected() throws Exception {
    startClient();
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
    startClient();
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

  //TODO More tests
  private String setupQueue(Set<String> messages) throws Exception {
    return setupQueue(messages, null);
  }

  private void startClient() throws Exception {
    if ("true".equalsIgnoreCase(System.getProperty("rabbitmq.local"))) {
      client = RabbitMQClient.create(vertx, config());
      CountDownLatch latch = new CountDownLatch(1);
      client.start(onSuccess(v -> {
        latch.countDown();
      }));
      awaitLatch(latch);
      channel = new ConnectionFactory().newConnection().createChannel();
    } else {
      // Use CloudAMQP
      RabbitMQOptions config = config().setUri(CLOUD_AMQP_URI);
      client = RabbitMQClient.create(vertx, config);
      CountDownLatch latch = new CountDownLatch(1);
      client.start(onSuccess(v -> {
        latch.countDown();
      }));
      awaitLatch(latch);
      ConnectionFactory factory = new ConnectionFactory();
      factory.setUri(CLOUD_AMQP_URI);
      channel = factory.newConnection().createChannel();
    }
  }

  private String setupQueue(Set<String> messages, String contentType) throws Exception {
    String queue = randomAlphaString(10);
    AMQP.Queue.DeclareOk ok = channel.queueDeclare(queue, false, false, true, null);
    assertNotNull(ok.getQueue());
    AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
      .contentType(contentType).contentEncoding("UTF-8").build();

    if (messages != null) {
      for (String msg : messages) {
        channel.basicPublish("", queue, properties, msg.getBytes("UTF-8"));
      }
    }
    return queue;
  }

  private Set<String> createMessages(int number) {
    Set<String> messages = new HashSet<>();
    for (int i = 0; i < number; i++) {
      messages.add(randomAlphaString(20));
    }
    return messages;
  }
}
