package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

import java.util.HashMap;
import java.util.Map;

public class Examples {

  public void createClientWithUri(Vertx vertx) {
    JsonObject config = new JsonObject();
    // full amqp uri
    config.put("uri", "amqp://xvjvsrrc:VbuL1atClKt7zVNQha0bnnScbNvGiqgb@moose.rmq.cloudamqp.com/xvjvsrrc");
    RabbitMQClient client = RabbitMQClient.create(vertx, config);
  }

  public void createClientWithManualParams(Vertx vertx) {
    JsonObject config = new JsonObject();
    // Each parameter is optional
    // The default parameter with be used if the parameter is not set
    config.put("user", "user1");
    config.put("password", "password1");
    config.put("host", "localhost");
    config.put("port", 5672);
    config.put("virtualHost", "vhost1");
    config.put("connectionTimeout", 60); // in seconds
    config.put("requestedHeartbeat", 60); // in seconds
    config.put("handshakeTimeout", 60); // in seconds
    config.put("requestedChannelMax", 5);
    config.put("networkRecoveryInterval", 5); // in seconds
    config.put("automaticRecoveryEnabled", true);

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

  //pass the additional config for the exchange as map, check RabbitMQ documentation for specific config parameters
  public void exchangeDeclareWithConfig(RabbitMQClient client) {

    Map<String, String> config = new HashMap<>();

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

}
