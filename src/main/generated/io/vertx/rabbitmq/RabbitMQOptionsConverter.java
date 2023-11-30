package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.rabbitmq.RabbitMQOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.RabbitMQOptions} original class using Vert.x codegen.
 */
public class RabbitMQOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RabbitMQOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "uri":
          if (member.getValue() instanceof String) {
            obj.setUri((String)member.getValue());
          }
          break;
        case "user":
          if (member.getValue() instanceof String) {
            obj.setUser((String)member.getValue());
          }
          break;
        case "password":
          if (member.getValue() instanceof String) {
            obj.setPassword((String)member.getValue());
          }
          break;
        case "host":
          if (member.getValue() instanceof String) {
            obj.setHost((String)member.getValue());
          }
          break;
        case "virtualHost":
          if (member.getValue() instanceof String) {
            obj.setVirtualHost((String)member.getValue());
          }
          break;
        case "port":
          if (member.getValue() instanceof Number) {
            obj.setPort(((Number)member.getValue()).intValue());
          }
          break;
        case "connectionTimeout":
          if (member.getValue() instanceof Number) {
            obj.setConnectionTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "requestedHeartbeat":
          if (member.getValue() instanceof Number) {
            obj.setRequestedHeartbeat(((Number)member.getValue()).intValue());
          }
          break;
        case "handshakeTimeout":
          if (member.getValue() instanceof Number) {
            obj.setHandshakeTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "requestedChannelMax":
          if (member.getValue() instanceof Number) {
            obj.setRequestedChannelMax(((Number)member.getValue()).intValue());
          }
          break;
        case "networkRecoveryInterval":
          if (member.getValue() instanceof Number) {
            obj.setNetworkRecoveryInterval(((Number)member.getValue()).longValue());
          }
          break;
        case "automaticRecoveryEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setAutomaticRecoveryEnabled((Boolean)member.getValue());
          }
          break;
        case "automaticRecoveryOnInitialConnection":
          if (member.getValue() instanceof Boolean) {
            obj.setAutomaticRecoveryOnInitialConnection((Boolean)member.getValue());
          }
          break;
        case "includeProperties":
          if (member.getValue() instanceof Boolean) {
            obj.setIncludeProperties((Boolean)member.getValue());
          }
          break;
        case "nioEnabled":
          break;
        case "useNio":
          if (member.getValue() instanceof Boolean) {
            obj.setUseNio((Boolean)member.getValue());
          }
          break;
        case "connectionName":
          if (member.getValue() instanceof String) {
            obj.setConnectionName((String)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(RabbitMQOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(RabbitMQOptions obj, java.util.Map<String, Object> json) {
    if (obj.getUri() != null) {
      json.put("uri", obj.getUri());
    }
    if (obj.getUser() != null) {
      json.put("user", obj.getUser());
    }
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    if (obj.getVirtualHost() != null) {
      json.put("virtualHost", obj.getVirtualHost());
    }
    json.put("port", obj.getPort());
    json.put("connectionTimeout", obj.getConnectionTimeout());
    json.put("requestedHeartbeat", obj.getRequestedHeartbeat());
    json.put("handshakeTimeout", obj.getHandshakeTimeout());
    json.put("requestedChannelMax", obj.getRequestedChannelMax());
    json.put("networkRecoveryInterval", obj.getNetworkRecoveryInterval());
    json.put("automaticRecoveryEnabled", obj.isAutomaticRecoveryEnabled());
    json.put("automaticRecoveryOnInitialConnection", obj.isAutomaticRecoveryOnInitialConnection());
    json.put("includeProperties", obj.getIncludeProperties());
    json.put("nioEnabled", obj.isNioEnabled());
    if (obj.getConnectionName() != null) {
      json.put("connectionName", obj.getConnectionName());
    }
  }
}
