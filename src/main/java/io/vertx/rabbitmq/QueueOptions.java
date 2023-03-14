package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Aimed to specify queue consumer settings when calling {@link RabbitMQClient#basicConsumer(String, QueueOptions)}
 */
@DataObject(generateConverter = true)
public class QueueOptions {

  private static final int DEFAULT_QUEUE_SIZE = Integer.MAX_VALUE;
  private static final boolean DEFAULT_AUTO_ACK = true;
  private static final boolean DEFAULT_KEEP_MOST_RECENT = false;

  private boolean autoAck = DEFAULT_AUTO_ACK;
  private boolean keepMostRecent = DEFAULT_KEEP_MOST_RECENT;
  private int maxInternalQueueSize = DEFAULT_QUEUE_SIZE;


  public QueueOptions() {
  }

  public QueueOptions(JsonObject json) {
    this();
    QueueOptionsConverter.fromJson(json, this);
  }

  /**
   * @param autoAck true if the server should consider messages
   *                acknowledged once delivered; false if the server should expect
   *                explicit acknowledgements
   */
  public QueueOptions setAutoAck(boolean autoAck) {
    this.autoAck = autoAck;
    return this;
  }

  /**
   * @param keepMostRecent {@code true} for discarding old messages instead of recent ones,
   *                       otherwise use {@code false}
   */
  public QueueOptions setKeepMostRecent(boolean keepMostRecent) {
    this.keepMostRecent = keepMostRecent;
    return this;
  }


  /**
   * @param maxInternalQueueSize the size of internal queue
   */
  public QueueOptions setMaxInternalQueueSize(int maxInternalQueueSize) {
    this.maxInternalQueueSize = maxInternalQueueSize;
    return this;
  }

  /**
   * @return true if the server should consider messages
   * acknowledged once delivered; false if the server should expect
   * explicit acknowledgements
   */
  public boolean isAutoAck() {
    return autoAck;
  }

  /**
   * @return the size of internal queue
   */
  public int maxInternalQueueSize() {
    return maxInternalQueueSize;
  }

  /**
   * @return {@code true} if old messages will be discarded instead of recent ones,
   * otherwise use {@code false}
   */
  public boolean isKeepMostRecent() {
    return keepMostRecent;
  }
}
