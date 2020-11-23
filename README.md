# RabbitMQ Client for Vert.x

[![Build Status](https://github.com/vert-x3/vertx-rabbitmq-client/workflows/CI/badge.svg?branch=master)](https://github.com/vert-x3/vertx-rabbitmq-client/actions?query=workflow%3ACI)

A Vert.x client allowing applications to interact with a RabbitMQ broker (AMQP 0.9.1)

# Getting Started

Please see the main documentation on the web-site for a full description:

* [Java documentation](https://vertx.io/docs/vertx-rabbitmq-client/java/)
* [JavaScript documentation](https://vertx.io/docs/vertx-rabbitmq-client/js/)
* [Kotlin documentation](https://vertx.io/docs/vertx-rabbitmq-client/kotlin/)
* [Groovy documentation](https://vertx.io/docs/vertx-rabbitmq-client/groovy/)
* [Ruby documentation](https://vertx.io/docs/vertx-rabbitmq-client/ruby/)

# Running the tests

By default the tests uses a cloud provided RabbitMQ instance.

```
% mvn test
```

You can run tests with a local RabbitMQ instance:

```
% mvn test -Prabbitmq.local
```

You will need to have RabbitMQ running with default ports on localhost for this to work.

You can setup a RabbitMQ instance with Docker:

```
docker run --rm --name vertx-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq
```
