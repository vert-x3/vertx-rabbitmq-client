package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.rabbitmq.RabbitMQPublisherConfirmation}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.RabbitMQPublisherConfirmation} original class using Vert.x codegen.
 */
public class RabbitMQPublisherConfirmationConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RabbitMQPublisherConfirmation obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "messageId":
          break;
        case "succeeded":
          break;
      }
    }
  }

  public static void toJson(RabbitMQPublisherConfirmation obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(RabbitMQPublisherConfirmation obj, java.util.Map<String, Object> json) {
    if (obj.getMessageId() != null) {
      json.put("messageId", obj.getMessageId());
    }
    json.put("succeeded", obj.isSucceeded());
  }
}
