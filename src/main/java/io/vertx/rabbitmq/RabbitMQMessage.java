package io.vertx.rabbitmq;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Envelope;
import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
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
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @CacheReturn
  Envelope envelope();

  /**
   * @return content header data for the message
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @CacheReturn
  BasicProperties properties();

  /**
   * @return the message count for messages obtained with {@link RabbitMQClient#basicGet(String, boolean, Handler)}
   */
  @CacheReturn
  Integer messageCount();
}
