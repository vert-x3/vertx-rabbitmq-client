package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

/**
 * A stream of messages from a rabbitmq queue
 */
@VertxGen
public interface RabbitMQueue extends ReadStream<JsonObject> {

  @Override
  RabbitMQueue exceptionHandler(Handler<Throwable> exceptionHandler);

  @Override
  RabbitMQueue handler(Handler<JsonObject> messageArrived);

  @Override
  RabbitMQueue pause();

  @Override
  RabbitMQueue resume();

  @Override
  RabbitMQueue endHandler(Handler<Void> endHandler);

  /**
   * How much messages can be in a internal queue.
   *
   * @param value new size of queue
   */
  void size(int value);

  /**
   * @return a consumer tag
   */
  String consumerTag();


  /**
   * Stop message consumption from a queue.
   * <p>
   * The operation is asynchronous. When consumption will be stopped, you can by notified via {@link RabbitMQueue#endHandler(Handler)}
   */
  void cancel();

  /**
   * Stop message consumption from a queue.
   * <p>
   * The operation is asynchronous. When consumption will be stopped, you can by notified via {@link RabbitMQueue#endHandler(Handler)}
   *
   * @param cancelResult contains information about operation status: success/fail.
   */
  void cancel(Handler<AsyncResult<Void>> cancelResult);
}
