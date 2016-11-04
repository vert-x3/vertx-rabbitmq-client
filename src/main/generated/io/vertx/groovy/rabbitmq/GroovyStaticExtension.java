package io.vertx.groovy.rabbitmq;
public class GroovyStaticExtension {
  public static io.vertx.rabbitmq.RabbitMQClient create(io.vertx.rabbitmq.RabbitMQClient j_receiver, io.vertx.core.Vertx vertx, java.util.Map<String, Object> config) {
    return io.vertx.lang.groovy.ConversionHelper.wrap(io.vertx.rabbitmq.RabbitMQClient.create(vertx,
      config != null ? io.vertx.lang.groovy.ConversionHelper.toJsonObject(config) : null));
  }
}
