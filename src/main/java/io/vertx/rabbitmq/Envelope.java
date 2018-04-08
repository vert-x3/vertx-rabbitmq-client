package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Like {@link com.rabbitmq.client.Envelope}.
 */
@VertxGen
public interface Envelope {

  /**
   * Get the delivery tag included in this parameter envelope
   *
   * @return the delivery tag
   */
  @CacheReturn
  long deliveryTag();

  /**
   * Get the redelivery flag included in this parameter envelope. This is a
   * hint as to whether this message may have been delivered before (but not
   * acknowledged). If the flag is not set, the message definitely has not
   * been delivered before. If it is set, it may have been delivered before.
   *
   * @return the redelivery flag
   */
  @CacheReturn
  boolean isRedelivery();

  /**
   * Get the name of the exchange included in this parameter envelope
   *
   * @return the exchange
   */
  @CacheReturn
  String exchange();

  /**
   * Get the routing key included in this parameter envelope
   *
   * @return the routing key
   */
  @CacheReturn
  String routingKey();
}
