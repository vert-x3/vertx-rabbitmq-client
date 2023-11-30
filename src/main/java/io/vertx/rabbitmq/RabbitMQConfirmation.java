package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author jtalbut
 */
@DataObject
@JsonGen(publicConverter = false)
public class RabbitMQConfirmation {

  private long channelInstance;
  private long deliveryTag;
  private boolean multiple;
  private boolean succeeded;

  public RabbitMQConfirmation(long channelInstance, long deliveryTag, boolean multiple, boolean succeeded) {
    this.channelInstance = channelInstance;
    this.deliveryTag = deliveryTag;
    this.multiple = multiple;
    this.succeeded = succeeded;
  }

  public RabbitMQConfirmation(JsonObject json) {
    RabbitMQConfirmationConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RabbitMQConfirmationConverter.toJson(this, json);
    return json;
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
