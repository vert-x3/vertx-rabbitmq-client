package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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
   * @return the name of the queue
   */
  String queueName();
  
  /**
   * Set the name of the queue.
   * This method is typically only required during a connectionEstablishedCallback when the queue name has changed.
   * @param name the name of the queue
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  RabbitMQConsumer setQueueName(String name);
  
  /**
   * @return a consumer tag
   */
  String consumerTag();

  /**
   * Stop message consumption from a queue.
   * <p>
   * The operation is asynchronous. When consumption is stopped, you can also be notified via {@link RabbitMQConsumer#endHandler(Handler)}
   * 
   * @return a future through which you can find out the operation status.
   */
  Future<Void> cancel();

  /**
   * Stop message consumption from a queue.
   * <p>
   * The operation is asynchronous. When consumption is stopped, you can also be notified via {@link RabbitMQConsumer#endHandler(Handler)}
   *
   * @param cancelResult contains information about operation status: success/fail.
   */
  void cancel(Handler<AsyncResult<Void>> cancelResult);

  /**
   * Return {@code true} if cancel() has been called.
   * @return {@code true}  if cancel() has been called. 
   */
  boolean isCancelled();
  
  /**
   * @return is the stream paused?
   */
  boolean isPaused();
  
  /**
   * Fetch the specified {@code amount} of elements. If the {@code ReadStream} has been paused, reading will
   * recommence with the specified {@code amount} of items, otherwise the specified {@code amount} will
   * be added to the current stream demand.
   *
   * @return a reference to this, so the API can be used fluently
   */
  RabbitMQConsumer fetch(long amount);
  
}
