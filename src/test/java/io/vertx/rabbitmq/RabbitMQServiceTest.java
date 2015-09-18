package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static io.vertx.test.core.TestUtils.randomAlphaString;
import static io.vertx.test.core.TestUtils.randomInt;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQServiceTest extends VertxTestBase {

  protected RabbitMQClient client;

  private Channel channel;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    client = RabbitMQClient.create(vertx, config());
    CountDownLatch latch = new CountDownLatch(1);
    client.start(onSuccess(v -> {
      latch.countDown();
    }));
    awaitLatch(latch);
    channel = new ConnectionFactory().newConnection().createChannel();
  }


  @Override
  protected void tearDown() throws Exception {
    channel.close();
    super.tearDown();
  }

  public JsonObject config() {
    return new JsonObject().put("connectionRetries", 0);
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
  public void testBasicConsumeNoAutoAck() throws Exception {
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
      if (latch.getCount() > 1) {
        long deliveryTag = json.getLong("deliveryTag");
        client.basicAck(deliveryTag, false, onSuccess(v -> {
        }));
      }
      latch.countDown();
    });

    client.basicConsume(q, "my.address", false, onSuccess(v -> {
    }));

    awaitLatch(latch);
    testComplete();
  }

  //TODO More tests

  private String setupQueue(Set<String> messages) throws Exception {
    String queue = randomAlphaString(10);
    AMQP.Queue.DeclareOk ok = channel.queueDeclare(queue, false, false, true, null);
    assertNotNull(ok.getQueue());
    if (messages != null) {
      for (String msg : messages) {
        channel.basicPublish("", queue, new AMQP.BasicProperties(), msg.getBytes("UTF-8"));
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
