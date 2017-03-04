# RabbitMQ Client for Vert.x

A Vert.x client allowing applications to interact with a RabbitMQ broker (AMQP 0.9.1)

**This service is experimental and the APIs are likely to change before settling down.**

# Getting Started

Please see the in source asciidoc documentation or the main documentation on the web-site for a full description:

* Web-site docs
* [Java in-source docs](../master/src/main/asciidoc/java/index.adoc)
* [JavaScript in-source docs](../master/src/main/asciidoc/js/index.adoc)
* [Groovy in-source docs](../master/src/main/asciidoc/groovy/index.adoc)
* [Ruby in-source docs](../master/src/main/asciidoc/ruby/index.adoc)

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
