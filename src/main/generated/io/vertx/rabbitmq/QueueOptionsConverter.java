package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.rabbitmq.QueueOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.QueueOptions} original class using Vert.x codegen.
 */
public class QueueOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, QueueOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "autoAck":
          if (member.getValue() instanceof Boolean) {
            obj.setAutoAck((Boolean)member.getValue());
          }
          break;
        case "consumerArguments":
          if (member.getValue() instanceof JsonObject) {
            java.util.Map<String, java.lang.Object> map = new java.util.LinkedHashMap<>();
            ((Iterable<java.util.Map.Entry<String, Object>>)member.getValue()).forEach(entry -> {
              if (entry.getValue() instanceof Object)
                map.put(entry.getKey(), entry.getValue());
            });
            obj.setConsumerArguments(map);
          }
          break;
        case "consumerExclusive":
          if (member.getValue() instanceof Boolean) {
            obj.setConsumerExclusive((Boolean)member.getValue());
          }
          break;
        case "consumerTag":
          if (member.getValue() instanceof String) {
            obj.setConsumerTag((String)member.getValue());
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
        case "noLocal":
          if (member.getValue() instanceof Boolean) {
            obj.setNoLocal((Boolean)member.getValue());
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
    if (obj.getConsumerArguments() != null) {
      JsonObject map = new JsonObject();
      obj.getConsumerArguments().forEach((key, value) -> map.put(key, value));
      json.put("consumerArguments", map);
    }
    json.put("consumerExclusive", obj.isConsumerExclusive());
    if (obj.getConsumerTag() != null) {
      json.put("consumerTag", obj.getConsumerTag());
    }
    json.put("keepMostRecent", obj.isKeepMostRecent());
    json.put("noLocal", obj.isNoLocal());
  }
}
