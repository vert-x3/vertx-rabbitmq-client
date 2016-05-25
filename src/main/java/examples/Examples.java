package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

public class Examples {

  public void createClient(Vertx vertx, JsonObject config) {
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
