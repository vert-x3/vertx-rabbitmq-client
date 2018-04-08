package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

/**
 * A stream of messages from a rabbitmq queue.
 */
@VertxGen
public interface RabbitMQConsumer extends ReadStream<RabbitMQMessage> {

  /**
   * Set an exception handler on the read stream.
   *
   * @param exceptionHandler the exception handler
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConsumer exceptionHandler(Handler<Throwable> exceptionHandler);

  /**
   * Set a message handler. As message appear in a queue, the handler will be called with the message.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConsumer handler(Handler<RabbitMQMessage> messageArrived);

  /**
   * Pause the stream of incoming messages from queue.
   * <p>
   * The messages will continue to arrive, but they will be stored in a internal queue.
   * If the queue size would exceed the limit provided by {@link RabbitMQConsumer#size(int)}, then incoming messages will be discarded.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConsumer pause();

  /**
   * Resume reading from a queue. Flushes internal queue.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConsumer resume();

  /**
   * Set an end handler. Once the stream has canceled successfully, the handler will be called.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConsumer endHandler(Handler<Void> endHandler);

  /**
   * @return a consumer tag
   */
  String consumerTag();

  /**
   * Stop message consumption from a queue.
   * <p>
   * The operation is asynchronous. When consumption will be stopped, you can by notified via {@link RabbitMQConsumer#endHandler(Handler)}
   */
  void cancel();

  /**
   * Stop message consumption from a queue.
   * <p>
   * The operation is asynchronous. When consumption will be stopped, you can by notified via {@link RabbitMQConsumer#endHandler(Handler)}
   *
   * @param cancelResult contains information about operation status: success/fail.
   */
  void cancel(Handler<AsyncResult<Void>> cancelResult);

  /**
   * @return is the stream paused?
   */
  boolean isPaused();
}
