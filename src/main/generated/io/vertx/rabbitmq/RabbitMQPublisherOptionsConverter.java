package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.rabbitmq.RabbitMQPublisherOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.RabbitMQPublisherOptions} original class using Vert.x codegen.
 */
public class RabbitMQPublisherOptionsConverter {

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RabbitMQPublisherOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "reconnectAttempts":
          if (member.getValue() instanceof Number) {
            obj.setReconnectAttempts(((Number)member.getValue()).intValue());
          }
          break;
        case "reconnectInterval":
          if (member.getValue() instanceof Number) {
            obj.setReconnectInterval(((Number)member.getValue()).longValue());
          }
          break;
        case "maxInternalQueueSize":
          if (member.getValue() instanceof Number) {
            obj.setMaxInternalQueueSize(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

   static void toJson(RabbitMQPublisherOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(RabbitMQPublisherOptions obj, java.util.Map<String, Object> json) {
    if (obj.getReconnectAttempts() != null) {
      json.put("reconnectAttempts", obj.getReconnectAttempts());
    }
    json.put("reconnectInterval", obj.getReconnectInterval());
    json.put("maxInternalQueueSize", obj.getMaxInternalQueueSize());
  }
}
