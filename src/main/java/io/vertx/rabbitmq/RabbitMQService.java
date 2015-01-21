package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.impl.RabbitMQServiceImpl;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@VertxGen
@ProxyGen
public interface RabbitMQService {

  static RabbitMQService create(Vertx vertx, JsonObject config) {
    return new RabbitMQServiceImpl(vertx, config);
  }

  static RabbitMQService createEventBusProxy(Vertx vertx, String address) {
    return ProxyHelper.createProxy(RabbitMQService.class, vertx, address);
  }

  //TODO: Think about splitting this out into different API's with specific roles (admin, pub, sub)
  //TODO: Simplify/Change name of API methods to match more vert.x type verbiage ?

  void basicGet(String queue, boolean autoAck, Handler<AsyncResult<JsonObject>> resultHandler);

  void basicConsume(String queue, String address, Handler<AsyncResult<Void>> resultHandler);

  void basicPublish(String exchange, String routingKey, JsonObject message, Handler<AsyncResult<Void>> resultHandler);

  void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler);

  void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler);

  void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Actively declare a server-named exclusive, autodelete, non-durable queue.
   *
   * @see com.rabbitmq.client.Channel#queueDeclare()
   */
  //TODO: Auto ?
  void queueDeclareAuto(Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Declare a queue
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   */
  void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String)
   */
  void queueDelete(String queue, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Delete a queue
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String, boolean, boolean)
   */
  void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Bind a queue to an exchange
   *
   * @see com.rabbitmq.client.Channel#queueBind(String, String, String)
   */
  void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  @ProxyIgnore
  void start();

  @ProxyIgnore
  void stop();
}
