package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.rabbitmq.RabbitMQConfirmation}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.RabbitMQConfirmation} original class using Vert.x codegen.
 */
public class RabbitMQConfirmationConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RabbitMQConfirmation obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "channelInstance":
          break;
        case "deliveryTag":
          break;
        case "multiple":
          break;
        case "succeeded":
          break;
      }
    }
  }

  public static void toJson(RabbitMQConfirmation obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(RabbitMQConfirmation obj, java.util.Map<String, Object> json) {
    json.put("channelInstance", obj.getChannelInstance());
    json.put("deliveryTag", obj.getDeliveryTag());
    json.put("multiple", obj.isMultiple());
    json.put("succeeded", obj.isSucceeded());
  }
}
