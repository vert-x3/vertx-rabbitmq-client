package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
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
  public void testMessageOrdering(TestContext ctx) throws IOException {

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
    client.basicConsumer(queueName).onComplete(consumerHandler -> {
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
      client.basicGet(q, true).onComplete(ctx.asyncAssertSuccess(msg -> {
        if (msg != null) {
          String body = msg.body().toString();
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
    Buffer message = Buffer.buffer(body);
    client.basicPublish("", q, message).onComplete(ctx.asyncAssertSuccess(v -> {
      client.basicGet(q, true).onComplete(ctx.asyncAssertSuccess(msg -> {
        ctx.assertNotNull(msg);
        ctx.assertEquals(body, msg.body().toString());
      }));
    }));
  }

  @Test
  public void testBasicPublishWithConfirm(TestContext ctx) throws Exception {
    String q = setupQueue(ctx, null);
    String body = randomAlphaString(100);
    Buffer message = Buffer.buffer(body);

    client.confirmSelect().onComplete(ctx.asyncAssertSuccess(v -> {
      client.basicPublish("", q, message).onComplete(ctx.asyncAssertSuccess(vv -> {
        client.waitForConfirms().onComplete(ctx.asyncAssertSuccess(vvv -> {
          client.basicGet(q, true).onComplete(ctx.asyncAssertSuccess(msg -> {
            ctx.assertNotNull(msg);
            ctx.assertEquals(body, msg.body().toString());
          }));
        }));
      }));
    }));
  }

  @Test
  public void testBasicPublishWithConfirmListener(TestContext ctx) throws Exception {
    String q = setupQueue(ctx, null);
    String body = randomAlphaString(100);
    Buffer message = Buffer.buffer(body);

    Async async = ctx.async();
    long deliveryTag[] = {0};

    client.addConfirmListener(1000).onComplete(v -> {
      v.result().handler(conf -> {
        long channelInstance = conf.getChannelInstance();
        ctx.assertEquals(1L, channelInstance);
        long dt = conf.getDeliveryTag();
        ctx.assertTrue(dt > 0);
        ctx.assertEquals(deliveryTag[0], dt);
        client.basicGet(q, true).onComplete(ctx.asyncAssertSuccess(msg -> {
          ctx.assertNotNull(msg);
          ctx.assertEquals(body, msg.body().toString());
          async.complete();
         }));
      });
      client.basicPublishWithDeliveryTag("", q, new AMQP.BasicProperties()
          , message
          , dt -> {
            deliveryTag[0] = dt;
          }).onComplete(ctx.asyncAssertSuccess(dt -> {
      }));
    });
  }

  @Test
  public void testBasicPublishWithConfirmAndTimeout(TestContext ctx) throws Exception {
    String q = setupQueue(ctx, null);
    String body = randomAlphaString(100);
    Buffer message = Buffer.buffer(body);

    client.confirmSelect().onComplete(ctx.asyncAssertSuccess(v -> {
      client.basicPublish("", q, message).onComplete(ctx.asyncAssertSuccess(vv -> {
        client.waitForConfirms(1000).onComplete(ctx.asyncAssertSuccess(vvv -> {
          client.basicGet(q, true).onComplete(ctx.asyncAssertSuccess(msg -> {
            ctx.assertNotNull(msg);
            ctx.assertEquals(body, msg.body().toString());
          }));
        }));
      }));
    }));
  }

  @Test
  public void testBasicPublishJson(TestContext ctx) throws Exception {
    String q = setupQueue(ctx, null);
    JsonObject body = new JsonObject().put("foo", randomAlphaString(5)).put("bar", randomInt());
    Buffer message = body.toBuffer();
    AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
      .contentType("application/json")
      .build();
    client.basicPublish("", q, props, message).onComplete(ctx.asyncAssertSuccess(v -> {
      client.basicGet(q, true).onComplete(ctx.asyncAssertSuccess(msg -> {
        ctx.assertNotNull(msg);
        JsonObject b = msg.body().toJsonObject();
        ctx.assertNotNull(b);
        ctx.assertFalse(body == b);
        ctx.assertEquals(body, b);
      }));
    }));
  }

  @Test
  public void testBasicConsumer(TestContext ctx) throws Exception {
    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async latch = ctx.async(count);

    client.basicConsumer(q).onComplete(ctx.asyncAssertSuccess(consumer -> {
      consumer.handler(msg -> {
        ctx.assertNotNull(msg);
        String body = msg.body().toString();
        ctx.assertNotNull(body);
        ctx.assertTrue(messages.contains(body));
        latch.countDown();
      });
    }));
  }

  @Test
  public void testBasicConsumerWithErrorHandler(TestContext ctx) throws Exception {
    int count = 1;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages, "application/json");

    Async latch = ctx.async(count);

    Handler<Throwable> errorHandler = throwable -> latch.countDown();

    client.basicConsumer(q).onComplete(ctx.asyncAssertSuccess(consumer -> {
      consumer.exceptionHandler(errorHandler);
      consumer.handler(json -> {
        throw new IllegalStateException("Getting message with malformed json");
      });
    }));
  }

  @Test
  public void testBasicConsumerNoAutoAck(TestContext ctx) throws Exception {

    int count = 3;
    Set<String> messages = createMessages(count);
    String q = setupQueue(ctx, messages);

    Async latch = ctx.async(count);

    client.basicConsumer(q, new QueueOptions().setAutoAck(false)).onComplete(consumerHandler -> {
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

  private void handleUnAckDelivery(TestContext ctx, Set<String> messages, Async async, RabbitMQMessage message) {
    String body = message.body().toString();
    ctx.assertTrue(messages.contains(body));
    Long deliveryTag = message.envelope().getDeliveryTag();
    log.info("message arrived: " + message.body().toString(message.properties().getContentEncoding()));
    log.info("redelivered? : " + message.envelope().isRedeliver());
    if (message.envelope().isRedeliver()) {
      client.basicAck(deliveryTag, false).onComplete(ctx.asyncAssertSuccess(v -> {
        // remove the message if is redeliver (unacked)
        messages.remove(body);
        async.countDown();
      }));
    } else {
      // send and Nack for every ready message
      client.basicNack(deliveryTag, false, true).onComplete(ctx.asyncAssertSuccess());
    }
  }

  @Test
  public void testQueueDeclareAndDelete(TestContext ctx) {
    String queueName = randomAlphaString(10);

    client.queueDeclare(queueName, false, false, true).onComplete(ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(result.getQueue(), queueName);

      client.queueDelete(queueName).onComplete(ctx.asyncAssertSuccess());
    }));
  }

  @Test
  public void testQueueDeclareAndDeleteWithConfig(TestContext ctx) {
    String queueName = randomAlphaString(10);
    JsonObject config = new JsonObject();
    config.put("x-message-ttl", 10_000L);

    client.queueDeclare(queueName, false, false, true, config).onComplete(ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(result.getQueue(), queueName);

      client.queueDelete(queueName).onComplete(ctx.asyncAssertSuccess());
    }));
  }

  //TODO: create an integration test with a test scenario
  @Test
  public void testDeclareExchangeWithAlternateExchange(TestContext ctx) throws Exception {
    String exName = randomAlphaString(10);
    JsonObject params = new JsonObject();
    params.put("alternate-exchange", "alt.ex");
    client.exchangeDeclare(exName, "direct", false, true, params).onComplete(ctx.asyncAssertSuccess());

  }

  //TODO: create an integration test with a test scenario
  @Test
  public void testDeclareExchangeWithDLX(TestContext ctx) throws Exception {
    String exName = randomAlphaString(10);
    JsonObject params = new JsonObject();
    params.put("x-dead-letter-exchange", "dlx.exchange");
    client.exchangeDeclare(exName, "direct", false, true, params).onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testIsOpenChannel(TestContext ctx) {

    boolean result = client.isOpenChannel();

    ctx.assertTrue(result);

    client.stop().onComplete(ctx.asyncAssertSuccess(v -> {
      ctx.assertFalse(client.isOpenChannel());
    }));
  }

  @Test
  public void testIsConnected(TestContext ctx) {

    boolean result = client.isConnected();

    ctx.assertTrue(result);

    client.stop().onComplete(ctx.asyncAssertSuccess(v -> {
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
      client.messageCount(queue).onComplete(ctx.asyncAssertSuccess(messageCount -> {
          ctx.assertEquals(count, messageCount.intValue());

          // remove the queue
          client.queueDelete(queue).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
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
    client.basicQos(amountOfUnAckMessages).onComplete(ctx.asyncAssertSuccess(v -> prefetchDone.complete()));
    prefetchDone.await();

    Set<String> messages = createMessages(count);
    String queue = setupQueue(ctx, messages);

    Async receivedExpectedNumberOfMessages = ctx.async(amountOfUnAckMessages);

    client.basicConsumer(queue, new QueueOptions().setAutoAck(false)).onComplete(ctx.asyncAssertSuccess(consumer -> {
      consumer.handler(msg -> {
        ctx.assertFalse(receivedExpectedNumberOfMessages.isCompleted());
        receivedExpectedNumberOfMessages.countDown();
      });
    }));

    receivedExpectedNumberOfMessages.awaitSuccess(15000);

    // At the point we are sure, that we have already received 2 messages.
    // But, if 3rd message will arrive the test will fail in the next second.
    Async async = ctx.async();
    vertx.setTimer(1000, spent -> async.countDown());
  }

  @Test
  public void testExchangeBind(TestContext ctx) throws Exception {

    String source = setupExchange(ctx, "fanout");
    String destination = setupExchange(ctx, "fanout");
    String routingKey = randomAlphaString(2);

    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.exchangeBind(destination, source, routingKey).onComplete(ctx.asyncAssertSuccess(v ->
        managementClient.getExchangeBindings(destination, source, ctx.asyncAssertSuccess(bindings -> {
          ctx.assertTrue(bindings.size() == 1);
          RabbitMQManagementClient.Binding binding = bindings.get(0);
          ctx.assertEquals(binding.getRoutingKey(), routingKey);
          ctx.assertTrue(binding.getArguments().isEmpty());

          client.exchangeDelete(source).onComplete(ctx.asyncAssertSuccess());
          client.exchangeDelete(destination).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
        })))
      )
    );
  }

  @Test
  public void testExchangeBindWithArguments(TestContext ctx) throws Exception {

    String source = setupExchange(ctx, "headers");
    String destination = setupExchange(ctx, "fanout");
    String routingKey = randomAlphaString(2);

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x-match", "any");
    arguments.put("name", "foo");

    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.exchangeBind(destination, source, routingKey, arguments).onComplete(ctx.asyncAssertSuccess(v ->
        managementClient.getExchangeBindings(destination, source, ctx.asyncAssertSuccess(bindings -> {
          ctx.assertTrue(bindings.size() == 1);
          RabbitMQManagementClient.Binding binding = bindings.get(0);
          ctx.assertEquals(binding.getRoutingKey(), routingKey);
          ctx.assertEquals(binding.getArguments(), arguments);

          client.exchangeDelete(source).onComplete(ctx.asyncAssertSuccess());
          client.exchangeDelete(destination).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
        })))
      )
    );
  }

  @Test
  public void testExchangeUnbind(TestContext ctx) throws Exception {

    String source = setupExchange(ctx, "fanout");
    String destination = setupExchange(ctx, "fanout");
    String routingKey = randomAlphaString(2);
    setupExchangeBinding(ctx, destination, source, routingKey);

    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.exchangeUnbind(destination, source, routingKey).onComplete(ctx.asyncAssertSuccess(v -> {
          managementClient.getExchangeBindings(destination, source, ctx.asyncAssertSuccess(bindings -> {
            ctx.assertTrue(bindings.isEmpty());

            client.exchangeDelete(source).onComplete(ctx.asyncAssertSuccess());
            client.exchangeDelete(destination).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
          }));
        })
      )
    );
  }

  @Test
  public void testExchangeUnbindWithArguments(TestContext ctx) throws Exception {

    String source = setupExchange(ctx, "headers");
    String destination = setupExchange(ctx, "fanout");

    String routingKey = randomAlphaString(2);
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x-match", "any");
    arguments.put("name", "foo");
    setupExchangeBinding(ctx, destination, source, routingKey, arguments);

    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.exchangeUnbind(destination, source, routingKey, arguments).onComplete(ctx.asyncAssertSuccess(v -> {
          managementClient.getExchangeBindings(destination, source, ctx.asyncAssertSuccess(bindings -> {
            ctx.assertTrue(bindings.isEmpty());

            client.exchangeDelete(source).onComplete(ctx.asyncAssertSuccess());
            client.exchangeDelete(destination).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
          }));
        })
      )
    );
  }

  @Test
  public void testQueueBind(TestContext ctx) throws Exception {

    String exchange = setupExchange(ctx, "fanout");
    String queue = setupQueue(ctx);
    String routingKey = randomAlphaString(2);

    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.queueBind(queue, exchange, routingKey).onComplete(ctx.asyncAssertSuccess(v ->
        managementClient.getQueueBindings(queue, exchange, ctx.asyncAssertSuccess(bindings -> {
          ctx.assertTrue(bindings.size() == 1);
          RabbitMQManagementClient.Binding binding = bindings.get(0);
          ctx.assertEquals(binding.getRoutingKey(), routingKey);
          ctx.assertTrue(binding.getArguments().isEmpty());

          client.exchangeDelete(exchange).onComplete(ctx.asyncAssertSuccess());
          client.queueDelete(queue).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
        })))
      )
    );
  }

  @Test
  public void testQueueBindWithArguments(TestContext ctx) throws Exception {

    String exchange = setupExchange(ctx, "headers");
    String queue = setupQueue(ctx);
    String routingKey = randomAlphaString(2);

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x-match", "any");
    arguments.put("name", "foo");

    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.queueBind(queue, exchange, routingKey, arguments).onComplete(ctx.asyncAssertSuccess(v -> {
          managementClient.getQueueBindings(queue, exchange, ctx.asyncAssertSuccess(bindings -> {
            ctx.assertTrue(bindings.size() == 1);
            RabbitMQManagementClient.Binding binding = bindings.get(0);
            ctx.assertEquals(binding.getRoutingKey(), routingKey);
            ctx.assertEquals(binding.getArguments(), arguments);

            client.exchangeDelete(exchange).onComplete(ctx.asyncAssertSuccess());
            client.queueDelete(queue).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
          }));
        })
      )
    );
  }

  @Test
  public void testQueueUnbind(TestContext ctx) throws Exception {

    String exchange = setupExchange(ctx, "fanout");
    String queue = setupQueue(ctx);
    String routingKey = randomAlphaString(2);
    setupQueueBinding(ctx, queue, exchange, routingKey);

    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.queueUnbind(queue, exchange, routingKey).onComplete(ctx.asyncAssertSuccess(v -> {
          managementClient.getQueueBindings(queue, exchange, ctx.asyncAssertSuccess(bindings -> {
            ctx.assertTrue(bindings.isEmpty());

            client.exchangeDelete(exchange).onComplete(ctx.asyncAssertSuccess());
            client.queueDelete(queue).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
          }));
        })
      )
    );
  }

  @Test
  public void testQueueUnbindWithArguments(TestContext ctx) throws Exception {

    String exchange = setupExchange(ctx, "headers");
    String queue = setupQueue(ctx);

    String routingKey = randomAlphaString(2);
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("x-match", "any");
    arguments.put("name", "foo");
    setupQueueBinding(ctx, queue, exchange, routingKey, arguments);

    Async async = ctx.async();

    vertx.setTimer(2000, t ->
      client.queueUnbind(queue, exchange, routingKey, arguments).onComplete(ctx.asyncAssertSuccess(v -> {
          managementClient.getQueueBindings(queue, exchange, ctx.asyncAssertSuccess(bindings -> {
            ctx.assertTrue(bindings.isEmpty());

            client.exchangeDelete(exchange).onComplete(ctx.asyncAssertSuccess());
            client.queueDelete(queue).onComplete(ctx.asyncAssertSuccess(json -> async.complete()));
          }));
        })
      )
    );
  }

  //TODO More tests
}
