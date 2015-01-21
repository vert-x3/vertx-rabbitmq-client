# RabbitMQ Service for Vert.x

A Vert.x service allowing applications to seamlessly interact with a RabbitMQ broker (amqp 0.9)

# Getting Started

## Maven

Add the following dependency to your maven project

```xml
<dependencies>
  <dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-rabbitmq-service</artifactId>
    <version>$version</version>
  </dependency>
</dependencies>
```

## Gradle ##

Add the following dependency to your gradle project

```groovy
dependencies {
  compile("io.vertx:vertx-rabbitmq-service:$version")
}
```

## Deploy as a service

The easiest way to get started is to deploy it as a service

```java
Vertx vertx = Vertx.vertx();
vertx.deployVerticle("service:io.vertx:rabbitmq-service");
```

This will make the RabbitMQService available as a [Service Proxy](#service-proxy) to be used throughout your application.

## Service Proxy

Assuming you already have the service deployed, to retrieve the RabbitMQService (from inside a Verticle for example)

```java
public class MyVerticle extends AbstractVerticle {

  @Override
  public void start() {
    RabbitMQService service = RabbitMQService.createEventBusProxy(vertx, "vertx.rabbitmq");
    ...
  }
```

This will create the service proxy allowing you to call the RabbitMQService API methods instead of having to send
messages over the event bus. See [Service Proxy](https://github.com/vert-x3/service-proxy) for more information.

# Operations

The following are some examples of the operations supported by the RabbitMQService API. Consult the javadoc/documentation
for detailed information on all API methods.

## Publish

Publish a message to a queue

```java
JsonObject message = new JsonObject().put("body", "Hello RabbitMQ, from Vert.x !");
service.basicPublish("", "my.queue", message, pubResult -> {
  if (pubResult.succeeded() {
    System.out.println("Message published !");
  } else {
    pubResult.cause().printStackTrace();
  }
});
```

## Consume

Consume messages from a queue

```java
// Create the event bus handler which messages will be sent to
vertx.eventBus().consumer("my.address", msg -> {
  JsonObject json = (JsonObject) msg.body();
  System.out.println("Got message: " + json.getString("body"));
});

// Setup the link between rabbitmq consumer and event bus address
service.basicConsume("my.queue", "my.address", consumeResult -> {
  if (consumeResult.succeeded()) {
    System.out.println("RabbitMQ consumer created !");
  } else {
    consumeResult.cause().printStackTrace();
  }
});
```

## Get

Will get a message from a queue

```java
service.basicGet("my.queue", true, getResult -> {
  if (getResult.succeeded()) {
    JsonObject msg = getResult.result();
    System.out.println("Got message: " + msg.getString("body"));
  } else {
    getResult.cause().printStackTrace();
  }
});
```
