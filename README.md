# RabbitMQ Client for Vert.x

[![Build Status](https://vertx.ci.cloudbees.com/buildStatus/icon?job=vert.x3-rabbitmq-client)](https://vertx.ci.cloudbees.com/view/vert.x-3/job/vert.x3-rabbitmq-client/)

A Vert.x client allowing applications to interact with a RabbitMQ broker (AMQP 0.9.1)

# Getting Started

Please see the in source asciidoc documentation or the main documentation on the web-site for a full description:

* [Java documentation](http://vertx.io/docs/vertx-rabbitmq-client/java/)
* [JavaScript documentation](http://vertx.io/docs/vertx-rabbitmq-client/js/)
* [Kotlin documentation](http://vertx.io/docs/vertx-rabbitmq-client/kotlin/)
* [Groovy documentation](http://vertx.io/docs/vertx-rabbitmq-client/groovy/)
* [Ruby documentation](http://vertx.io/docs/vertx-rabbitmq-client/ruby/)

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
