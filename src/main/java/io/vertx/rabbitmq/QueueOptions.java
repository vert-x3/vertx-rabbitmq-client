package io.vertx.rabbitmq;

import java.util.Map;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

/**
 * Aimed to specify queue consumer settings when calling {@link RabbitMQClient#basicConsumer(String, QueueOptions, Handler)}
 */
@DataObject
@JsonGen(publicConverter = false)
public class QueueOptions {

  private static final int DEFAULT_QUEUE_SIZE = Integer.MAX_VALUE;
  private static final boolean DEFAULT_AUTO_ACK = true;
  private static final boolean DEFAULT_KEEP_MOST_RECENT = false;
  private static final boolean DEFAULT_NO_LOCAL = false;
  private static final boolean DEFAULT_CONSUMER_EXCLUSIVE = false;
  private static final String DEFAULT_CONSUMER_TAG = "";

  private boolean autoAck = DEFAULT_AUTO_ACK;
  private boolean keepMostRecent = DEFAULT_KEEP_MOST_RECENT;
  private int maxInternalQueueSize = DEFAULT_QUEUE_SIZE;
  private boolean noLocal = DEFAULT_NO_LOCAL;
  private boolean consumerExclusive = DEFAULT_CONSUMER_EXCLUSIVE;
  private String consumerTag = DEFAULT_CONSUMER_TAG;
  private Map<String, Object> consumerArguments = null;


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

  public boolean isNoLocal() {
    return noLocal;
  }

  public void setNoLocal(boolean noLocal) {
    this.noLocal = noLocal;
  }

  public boolean isConsumerExclusive() {
    return consumerExclusive;
  }

  public void setConsumerExclusive(boolean consumerExclusive) {
    this.consumerExclusive = consumerExclusive;
  }

  public String getConsumerTag() {
    return consumerTag;
  }

  public void setConsumerTag(String consumerTag) {
    this.consumerTag = consumerTag;
  }

  public Map<String, Object> getConsumerArguments() {
    return consumerArguments;
  }

  public void setConsumerArguments(Map<String, Object> consumerArguments) {
    this.consumerArguments = consumerArguments;
  }
}
