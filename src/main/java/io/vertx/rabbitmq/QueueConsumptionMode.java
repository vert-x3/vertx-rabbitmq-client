package io.vertx.rabbitmq;

import io.vertx.core.Handler;

/**
 * Back-pressure strategies for {@link RabbitMQueue}.
 * <p>
 * Should be provided once when queue is created by calling {@link RabbitMQClient#basicConsumer(String, Handler)}
 */
public enum QueueConsumptionMode {

  /**
   * Discard all incoming from a queue messages.
   * <p>
   * The strategy is used by default.
   */
  DISCARD_ALL,

  /**
   * All incoming messages will be stored in an internal queue.
   * When the queue will be filled, income messages will be discarded.
   */
  BUFFER,

  /**
   * All incoming messages will be stored in an internal queue.
   * When the queue will be filled, income messages will be put in an internal queue replacing old ones.
   */
  BUFFER_REPLACE_OLD_WITH_NEW
}
