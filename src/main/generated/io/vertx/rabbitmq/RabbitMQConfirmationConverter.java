package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.rabbitmq.RabbitMQConfirmation}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.RabbitMQConfirmation} original class using Vert.x codegen.
 */
public class RabbitMQConfirmationConverter {

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RabbitMQConfirmation obj) {
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

   static void toJson(RabbitMQConfirmation obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(RabbitMQConfirmation obj, java.util.Map<String, Object> json) {
    json.put("channelInstance", obj.getChannelInstance());
    json.put("deliveryTag", obj.getDeliveryTag());
    json.put("multiple", obj.isMultiple());
    json.put("succeeded", obj.isSucceeded());
  }
}
