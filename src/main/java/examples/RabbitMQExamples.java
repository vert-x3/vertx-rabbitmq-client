package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;

import java.util.HashMap;
import java.util.Map;

public class RabbitMQExamples {

  public void createClientWithUri(Vertx vertx) {
    RabbitMQOptions config = new RabbitMQOptions();
    // full amqp uri
    config.setUri("amqp://xvjvsrrc:VbuL1atClKt7zVNQha0bnnScbNvGiqgb@moose.rmq.cloudamqp.com/xvjvsrrc");
    RabbitMQClient client = RabbitMQClient.create(vertx, config);
  }

  public void createClientWithManualParams(Vertx vertx) {
    RabbitMQOptions config = new RabbitMQOptions();
    // Each parameter is optional
    // The default parameter with be used if the parameter is not set
    config.setUser("user1");
    config.setPassword("password1");
    config.setHost("localhost");
    config.setPort(5672);
    config.setVirtualHost("vhost1");
    config.setConnectionTimeout(6000); // in milliseconds
    config.setRequestedHeartbeat(60); // in seconds
    config.setHandshakeTimeout(6000); // in milliseconds
    config.setRequestedChannelMax(5);
    config.setNetworkRecoveryInterval(500); // in milliseconds
    config.setAutomaticRecoveryEnabled(true);

    RabbitMQClient client = RabbitMQClient.create(vertx, config);
  }

  public void basicPublish(RabbitMQClient client) {
    JsonObject message = new JsonObject().put("body", "Hello RabbitMQ, from Vert.x !");
    client.basicPublish("", "my.queue", message, pubResult -> {
      if (pubResult.succeeded()) {
        System.out.println("Message published !");
      } else {
        pubResult.cause().printStackTrace();
      }
    });
  }

  public void basicPublishWithConfirm(RabbitMQClient client) {
    JsonObject message = new JsonObject().put("body", "Hello RabbitMQ, from Vert.x !");

    // Put the channel in confirm mode. This can be done once at init.
    client.confirmSelect(confirmResult -> {
      if(confirmResult.succeeded()) {
        client.basicPublish("", "my.queue", message, pubResult -> {
          if (pubResult.succeeded()) {
            // Check the message got confirmed by the broker.
            client.waitForConfirms(waitResult -> {
              if(waitResult.succeeded())
                System.out.println("Message published !");
              else
                waitResult.cause().printStackTrace();
            });
          } else {
            pubResult.cause().printStackTrace();
          }
        });
      } else {
        confirmResult.cause().printStackTrace();
      }
    });

  }


  public void basicConsume(Vertx vertx, RabbitMQClient client) {
    // Create the event bus handler which messages will be sent to
    vertx.eventBus().consumer("my.address", msg -> {
      JsonObject json = (JsonObject) msg.body();
      System.out.println("Got message: " + json.getString("body"));
    });

    // Setup the link between rabbitmq consumer and event bus address
    client.basicConsume("my.queue", "my.address", consumeResult -> {
      if (consumeResult.succeeded()) {
        System.out.println("RabbitMQ consumer created !");
      } else {
        consumeResult.cause().printStackTrace();
      }
    });
  }

  public void basicConsumer(Vertx vertx, RabbitMQClient client) {
    client.basicConsumer("my.queue", rabbitMQConsumerAsyncResult -> {
      if (rabbitMQConsumerAsyncResult.succeeded()) {
        System.out.println("RabbitMQ consumer created !");
        RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
        mqConsumer.handler(message -> {
          System.out.println("Got message: " + message.body().toString());
        });
      } else {
        rabbitMQConsumerAsyncResult.cause().printStackTrace();
      }
    });
  }

  public void basicConsumerOptions(Vertx vertx, RabbitMQClient client) {
    QueueOptions options = new QueueOptions()
      .setMaxInternalQueueSize(1000)
      .setKeepMostRecent(true)
      .setBuffer(true);

    client.basicConsumer("my.queue", options, rabbitMQConsumerAsyncResult -> {
      if (rabbitMQConsumerAsyncResult.succeeded()) {
        System.out.println("RabbitMQ consumer created !");
      } else {
        rabbitMQConsumerAsyncResult.cause().printStackTrace();
      }
    });
  }

  public void pauseAndResumeConsumer(RabbitMQConsumer consumer){
    consumer.pause();
    consumer.resume();
  }

  public void endHandlerConsumer(RabbitMQConsumer rabbitMQConsumer) {
    rabbitMQConsumer.endHandler(v -> {
      System.out.println("It is the end of the stream");
    });
  }

  public void cancelConsumer(RabbitMQConsumer rabbitMQConsumer) {
    rabbitMQConsumer.cancel(cancelResult -> {
      if (cancelResult.succeeded()) {
        System.out.println("Consumption successfully stopped");
      } else {
        System.out.println("Tired in attempt to stop consumption");
        cancelResult.cause().printStackTrace();
      }
    });
  }

  public void exceptionHandler(RabbitMQConsumer consumer) {
    consumer.exceptionHandler(e -> {
      System.out.println("An exception occurred in the process of message handling");
      e.printStackTrace();
    });
  }

  public void consumerTag(RabbitMQConsumer consumer) {
    String consumerTag = consumer.consumerTag();
    System.out.println("Consumer tag is: " + consumerTag);
  }

  public void getMessage(RabbitMQClient client) {
    client.basicGet("my.queue", true, getResult -> {
      if (getResult.succeeded()) {
        JsonObject msg = getResult.result();
        System.out.println("Got message: " + msg.getString("body"));
      } else {
        getResult.cause().printStackTrace();
      }
    });
  }

  //pass the additional config for the exchange as JSON, check RabbitMQ documentation for specific config parameters
  public void exchangeDeclareWithConfig(RabbitMQClient client) {

    JsonObject config = new JsonObject();

    config.put("x-dead-letter-exchange", "my.deadletter.exchange");
    config.put("alternate-exchange", "my.alternate.exchange");
    // ...
    client.exchangeDeclare("my.exchange", "fanout", true, false, config, onResult -> {
      if (onResult.succeeded()) {
        System.out.println("Exchange successfully declared with config");
      } else {
        onResult.cause().printStackTrace();
      }
    });
  }

  public void consumeWithManualAck(Vertx vertx, RabbitMQClient client) {
    // Create the event bus handler which messages will be sent to
    vertx.eventBus().consumer("my.address", msg -> {
      JsonObject json = (JsonObject) msg.body();
      System.out.println("Got message: " + json.getString("body"));
      // ack
      client.basicAck(json.getLong("deliveryTag"), false, asyncResult -> {
      });
    });

    // Setup the link between rabbitmq consumer and event bus address
    client.basicConsume("my.queue", "my.address", false, consumeResult -> {
      if (consumeResult.succeeded()) {
        System.out.println("RabbitMQ consumer created !");
      } else {
        consumeResult.cause().printStackTrace();
      }
    });
  }

  //pass the additional config for the queue as JSON, check RabbitMQ documentation for specific config parameters
  public void queueDeclareWithConfig(RabbitMQClient client) {
    JsonObject config = new JsonObject();
    config.put("x-message-ttl", 10_000L);

    client.queueDeclare("my-queue", true, false, true, config, queueResult -> {
      if (queueResult.succeeded()) {
        System.out.println("Queue declared!");
      } else {
        System.err.println("Queue failed to be declared!");
        queueResult.cause().printStackTrace();
      }
    });

  }

}
