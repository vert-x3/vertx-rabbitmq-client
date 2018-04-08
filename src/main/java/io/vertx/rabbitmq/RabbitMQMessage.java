package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;

/**
 * Represent a message received message received in a rabbitmq-queue.
 */
@VertxGen
public interface RabbitMQMessage {

  /**
   * @return the message body
   */
  @CacheReturn
  Buffer body();

  /**
   * @return the <i>consumer tag</i> associated with the consumer
   */
  @CacheReturn
  String consumerTag();

  /**
   * @return packaging data for the message
   */
  @CacheReturn
  Envelope envelope();

  /**
   * @return content header data for the message
   */
  @CacheReturn
  BasicProperties properties();
}
