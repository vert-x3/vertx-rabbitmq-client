package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.rabbitmq.QueueOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.QueueOptions} original class using Vert.x codegen.
 */
public class QueueOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, QueueOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "autoAck":
          if (member.getValue() instanceof Boolean) {
            obj.setAutoAck((Boolean)member.getValue());
          }
          break;
        case "keepMostRecent":
          if (member.getValue() instanceof Boolean) {
            obj.setKeepMostRecent((Boolean)member.getValue());
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

  public static void toJson(QueueOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(QueueOptions obj, java.util.Map<String, Object> json) {
    json.put("autoAck", obj.isAutoAck());
    json.put("keepMostRecent", obj.isKeepMostRecent());
  }
}
