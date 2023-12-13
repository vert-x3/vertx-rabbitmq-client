package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.rabbitmq.RabbitMQPublisherConfirmation}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.RabbitMQPublisherConfirmation} original class using Vert.x codegen.
 */
public class RabbitMQPublisherConfirmationConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RabbitMQPublisherConfirmation obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "deliveryTag":
          break;
        case "messageId":
          break;
        case "succeeded":
          break;
      }
    }
  }

   static void toJson(RabbitMQPublisherConfirmation obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(RabbitMQPublisherConfirmation obj, java.util.Map<String, Object> json) {
    json.put("deliveryTag", obj.getDeliveryTag());
    if (obj.getMessageId() != null) {
      json.put("messageId", obj.getMessageId());
    }
    json.put("succeeded", obj.isSucceeded());
  }
}
