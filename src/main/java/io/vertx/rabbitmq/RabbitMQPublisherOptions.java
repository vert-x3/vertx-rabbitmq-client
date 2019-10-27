package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;


/**
 * RabbitMQ client options, most
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class RabbitMQPublisherOptions {

  /**
   * The default connection retry delay = {@code 10000}
   */
  public static final long DEFAULT_RECONNECT_INTERVAL = 1000L;

  /**
   * The default connection retries = {@code Integer.MAX_VALUE}
   */
  public static final Integer DEFAULT_RECONNECT_ATTEMPTS = Integer.MAX_VALUE;

  /**
   * The default internal queue size = {@code Integer.MAX_VALUE}
   */
  private static final int DEFAULT_QUEUE_SIZE = Integer.MAX_VALUE;
  
  private Integer reconnectAttempts = 	DEFAULT_RECONNECT_ATTEMPTS;
  private long reconnectInterval = DEFAULT_RECONNECT_INTERVAL;
  private int maxInternalQueueSize = DEFAULT_QUEUE_SIZE;

  
  public RabbitMQPublisherOptions() {
  }

  public RabbitMQPublisherOptions(JsonObject json) {
    this();
    RabbitMQPublisherOptionsConverter.fromJson(json, this);
  }

  public RabbitMQPublisherOptions(RabbitMQPublisherOptions that) {
    reconnectAttempts = that.reconnectAttempts;
    reconnectInterval = that.reconnectInterval;
    maxInternalQueueSize = that.maxInternalQueueSize;
  }

  /**
   * @return the number of reconnect attempts
   */
  public Integer getReconnectAttempts() {
    return reconnectAttempts;
  }

  /**
   * Set the number of reconnect attempts to attempt when connecting, the {@code null} value disables it.
   *
   * @param reconnectAttempts the number of retries
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQPublisherOptions setReconnectAttempts(Integer reconnectAttempts) {
    this.reconnectAttempts = reconnectAttempts;
    return this;
  }

  /**
   * @return the delay in milliseconds between connection retries
   */
  public long getReconnectInterval() {
    return reconnectInterval;
  }

  /**
   * Set the delay in milliseconds between connection retries.
   *
   * @param reconnectInterval the delay in milliseconds
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQPublisherOptions setReconnectInterval(long reconnectInterval) {
    this.reconnectInterval = reconnectInterval;
    return this;
  }

  /**
   * @return the size of internal queue
   */
  public int getMaxInternalQueueSize() {
    return maxInternalQueueSize;
  }
  
  /**
   * @param maxInternalQueueSize the size of internal queue
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQPublisherOptions setMaxInternalQueueSize(int maxInternalQueueSize) {
    this.maxInternalQueueSize = maxInternalQueueSize;
    return this;
  }
  
}
