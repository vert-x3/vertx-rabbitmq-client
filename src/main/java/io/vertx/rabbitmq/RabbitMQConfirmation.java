package io.vertx.rabbitmq;

/**
 *
 * @author jtalbut
 */
public class RabbitMQConfirmation {
  
  private final long channelInstance;
  private final long deliveryTag;
  private final boolean multiple;
  private final boolean succeeded;

  public RabbitMQConfirmation(long channelInstance, long deliveryTag, boolean multiple, boolean succeeded) {
    this.channelInstance = channelInstance;
    this.deliveryTag = deliveryTag;
    this.multiple = multiple;
    this.succeeded = succeeded;
  }

  public long getChannelInstance() {
    return channelInstance;
  }

  public long getDeliveryTag() {
    return deliveryTag;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public boolean isSucceeded() {
    return succeeded;
  }

}
