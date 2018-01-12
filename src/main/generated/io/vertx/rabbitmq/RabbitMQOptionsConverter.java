/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link io.vertx.rabbitmq.RabbitMQOptions}.
 *
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.RabbitMQOptions} original class using Vert.x codegen.
 */
public class RabbitMQOptionsConverter {

  public static void fromJson(JsonObject json, RabbitMQOptions obj) {
    if (json.getValue("automaticRecoveryEnabled") instanceof Boolean) {
      obj.setAutomaticRecoveryEnabled((Boolean)json.getValue("automaticRecoveryEnabled"));
    }
    if (json.getValue("connectionRetries") instanceof Number) {
      obj.setConnectionRetries(((Number)json.getValue("connectionRetries")).intValue());
    }
    if (json.getValue("connectionRetryDelay") instanceof Number) {
      obj.setConnectionRetryDelay(((Number)json.getValue("connectionRetryDelay")).longValue());
    }
    if (json.getValue("connectionTimeout") instanceof Number) {
      obj.setConnectionTimeout(((Number)json.getValue("connectionTimeout")).intValue());
    }
    if (json.getValue("handshakeTimeout") instanceof Number) {
      obj.setHandshakeTimeout(((Number)json.getValue("handshakeTimeout")).intValue());
    }
    if (json.getValue("host") instanceof String) {
      obj.setHost((String)json.getValue("host"));
    }
    if (json.getValue("includeProperties") instanceof Boolean) {
      obj.setIncludeProperties((Boolean)json.getValue("includeProperties"));
    }
    if (json.getValue("networkRecoveryInterval") instanceof Number) {
      obj.setNetworkRecoveryInterval(((Number)json.getValue("networkRecoveryInterval")).longValue());
    }
    if (json.getValue("password") instanceof String) {
      obj.setPassword((String)json.getValue("password"));
    }
    if (json.getValue("port") instanceof Number) {
      obj.setPort(((Number)json.getValue("port")).intValue());
    }
    if (json.getValue("requestedChannelMax") instanceof Number) {
      obj.setRequestedChannelMax(((Number)json.getValue("requestedChannelMax")).intValue());
    }
    if (json.getValue("requestedHeartbeat") instanceof Number) {
      obj.setRequestedHeartbeat(((Number)json.getValue("requestedHeartbeat")).intValue());
    }
    if (json.getValue("uri") instanceof String) {
      obj.setUri((String)json.getValue("uri"));
    }
    if (json.getValue("user") instanceof String) {
      obj.setUser((String)json.getValue("user"));
    }
    if (json.getValue("virtualHost") instanceof String) {
      obj.setVirtualHost((String)json.getValue("virtualHost"));
    }
  }

  public static void toJson(RabbitMQOptions obj, JsonObject json) {
    json.put("automaticRecoveryEnabled", obj.isAutomaticRecoveryEnabled());
    if (obj.getConnectionRetries() != null) {
      json.put("connectionRetries", obj.getConnectionRetries());
    }
    json.put("connectionRetryDelay", obj.getConnectionRetryDelay());
    json.put("connectionTimeout", obj.getConnectionTimeout());
    json.put("handshakeTimeout", obj.getHandshakeTimeout());
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    json.put("includeProperties", obj.getIncludeProperties());
    json.put("networkRecoveryInterval", obj.getNetworkRecoveryInterval());
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    json.put("port", obj.getPort());
    json.put("requestedChannelMax", obj.getRequestedChannelMax());
    json.put("requestedHeartbeat", obj.getRequestedHeartbeat());
    if (obj.getUri() != null) {
      json.put("uri", obj.getUri());
    }
    if (obj.getUser() != null) {
      json.put("user", obj.getUser());
    }
    if (obj.getVirtualHost() != null) {
      json.put("virtualHost", obj.getVirtualHost());
    }
  }
}