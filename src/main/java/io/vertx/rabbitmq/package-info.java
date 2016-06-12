/**
 *
 * = RabbitMQ Client for Vert.x
 *
 * A Vert.x client allowing applications to interact with a RabbitMQ broker (AMQP 0.9.1)
 *
 * **This service is experimental and the APIs are likely to change before settling down.**
 *
 * == Getting Started
 *
 * === Maven
 *
 * Add the following dependency to your maven project
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 *   <groupId>${maven.groupId}</groupId>
 *   <artifactId>${maven.artifactId}</artifactId>
 *   <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * === Gradle
 *
 * Add the following dependency to your gradle project
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * dependencies {
 *   compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * }
 * ----
 *
 * === Create a client
 *
 * You can create a client instance as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#createClient}
 * ----
 *
 * == Operations
 *
 * The following are some examples of the operations supported by the RabbitMQService API.
 * Consult the javadoc/documentation for detailed information on all API methods.
 *
 * === Publish
 *
 * Publish a message to a queue
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#basicPublish}
 * ----
 *
 * === Consume
 *
 * Consume messages from a queue
 *
 * [source,$lang]
 * ----
 * // Create the event bus handler which messages will be sent to
 * {@link examples.Examples#basicConsume}
 * ----
 *
 * === Get
 *
 * Will get a message from a queue
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#getMessage}
 * ----
 *
 * === Consume messages without auto-ack
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#consumeWithManualAck}
 * ----
 *
 * == Running the tests
 *
 * You will need to have RabbitMQ installed and running with default ports on localhost for this to work.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-rabbitmq", groupPackage = "io.vertx")
package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
