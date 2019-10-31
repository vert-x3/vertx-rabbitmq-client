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
  public static final long DEFAULT_CONNECTION_RETRY_DELAY = 1000L;

  /**
   * The default connection retries = {@code Integer.MAX_VALUE}
   */
  public static final Integer DEFAULT_CONNECTION_RETRIES = Integer.MAX_VALUE;

  /**
   * The default internal queue size = {@code Integer.MAX_VALUE}
   */
  private static final int DEFAULT_QUEUE_SIZE = Integer.MAX_VALUE;
  
  private Integer connectionRetries = DEFAULT_CONNECTION_RETRIES;
  private long connectionRetryDelay = DEFAULT_CONNECTION_RETRY_DELAY;
  private int maxInternalQueueSize = DEFAULT_QUEUE_SIZE;

  
  public RabbitMQPublisherOptions() {
  }

  public RabbitMQPublisherOptions(JsonObject json) {
    this();
    RabbitMQPublisherOptionsConverter.fromJson(json, this);
  }

  public RabbitMQPublisherOptions(RabbitMQPublisherOptions that) {
    connectionRetries = that.connectionRetries;
    connectionRetryDelay = that.connectionRetryDelay;
    maxInternalQueueSize = that.maxInternalQueueSize;
  }

  /**
   * @return the number of connection retries
   */
  public Integer getConnectionRetries() {
    return connectionRetries;
  }

  /**
   * Set the number of connection retries to attempt when connecting, the {@code null} value disables it.
   *
   * @param connectionRetries the number of retries
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQPublisherOptions setConnectionRetries(Integer connectionRetries) {
    this.connectionRetries = connectionRetries;
    return this;
  }

  /**
   * @return the delay in milliseconds between connection retries
   */
  public long getConnectionRetryDelay() {
    return connectionRetryDelay;
  }

  /**
   * Set the delay in milliseconds between connection retries.
   *
   * @param connectionRetryDelay the delay in milliseconds
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQPublisherOptions setConnectionRetryDelay(long connectionRetryDelay) {
    this.connectionRetryDelay = connectionRetryDelay;
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
