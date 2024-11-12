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
        case "activityLogDataFormat":
          if (member.getValue() instanceof String) {
            obj.setActivityLogDataFormat(io.netty.handler.logging.ByteBufFormat.valueOf((String)member.getValue()));
          }
          break;
        case "applicationLayerProtocols":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<java.lang.String> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                list.add((String)item);
            });
            obj.setApplicationLayerProtocols(list);
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
        case "connectTimeout":
          if (member.getValue() instanceof Number) {
            obj.setConnectTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "connectionName":
          if (member.getValue() instanceof String) {
            obj.setConnectionName((String)member.getValue());
          }
          break;
        case "connectionTimeout":
          if (member.getValue() instanceof Number) {
            obj.setConnectionTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "crlPaths":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                obj.addCrlPath((String)item);
            });
          }
          break;
        case "crlValues":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                obj.addCrlValue(io.vertx.core.buffer.Buffer.buffer(BASE64_DECODER.decode((String)item)));
            });
          }
          break;
        case "enabledCipherSuites":
          if (member.getValue() instanceof JsonArray) {
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                obj.addEnabledCipherSuite((String)item);
            });
          }
          break;
        case "enabledSecureTransportProtocols":
          if (member.getValue() instanceof JsonArray) {
            java.util.LinkedHashSet<java.lang.String> list =  new java.util.LinkedHashSet<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                list.add((String)item);
            });
            obj.setEnabledSecureTransportProtocols(list);
          }
          break;
        case "handshakeTimeout":
          if (member.getValue() instanceof Number) {
            obj.setHandshakeTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "host":
          if (member.getValue() instanceof String) {
            obj.setHost((String)member.getValue());
          }
          break;
        case "hostnameVerificationAlgorithm":
          if (member.getValue() instanceof String) {
            obj.setHostnameVerificationAlgorithm((String)member.getValue());
          }
          break;
        case "idleTimeout":
          if (member.getValue() instanceof Number) {
            obj.setIdleTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "idleTimeoutUnit":
          if (member.getValue() instanceof String) {
            obj.setIdleTimeoutUnit(java.util.concurrent.TimeUnit.valueOf((String)member.getValue()));
          }
          break;
        case "includeProperties":
          if (member.getValue() instanceof Boolean) {
            obj.setIncludeProperties((Boolean)member.getValue());
          }
          break;
        case "jdkSslEngineOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setJdkSslEngineOptions(new io.vertx.core.net.JdkSSLEngineOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "keyStoreOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setKeyStoreOptions(new io.vertx.core.net.JksOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "localAddress":
          if (member.getValue() instanceof String) {
            obj.setLocalAddress((String)member.getValue());
          }
          break;
        case "logActivity":
          if (member.getValue() instanceof Boolean) {
            obj.setLogActivity((Boolean)member.getValue());
          }
          break;
        case "metricsName":
          if (member.getValue() instanceof String) {
            obj.setMetricsName((String)member.getValue());
          }
          break;
        case "networkRecoveryInterval":
          if (member.getValue() instanceof Number) {
            obj.setNetworkRecoveryInterval(((Number)member.getValue()).longValue());
          }
          break;
        case "nioEnabled":
          break;
        case "nonProxyHosts":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<java.lang.String> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                list.add((String)item);
            });
            obj.setNonProxyHosts(list);
          }
          break;
        case "openSslEngineOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setOpenSslEngineOptions(new io.vertx.core.net.OpenSSLEngineOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "password":
          if (member.getValue() instanceof String) {
            obj.setPassword((String)member.getValue());
          }
          break;
        case "pemKeyCertOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPemKeyCertOptions(new io.vertx.core.net.PemKeyCertOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "pemTrustOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPemTrustOptions(new io.vertx.core.net.PemTrustOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "pfxKeyCertOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPfxKeyCertOptions(new io.vertx.core.net.PfxOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "pfxTrustOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setPfxTrustOptions(new io.vertx.core.net.PfxOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "port":
          if (member.getValue() instanceof Number) {
            obj.setPort(((Number)member.getValue()).intValue());
          }
          break;
        case "proxyOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setProxyOptions(new io.vertx.core.net.ProxyOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "readIdleTimeout":
          if (member.getValue() instanceof Number) {
            obj.setReadIdleTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "receiveBufferSize":
          if (member.getValue() instanceof Number) {
            obj.setReceiveBufferSize(((Number)member.getValue()).intValue());
          }
          break;
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
        case "registerWriteHandler":
          if (member.getValue() instanceof Boolean) {
            obj.setRegisterWriteHandler((Boolean)member.getValue());
          }
          break;
        case "requestedChannelMax":
          if (member.getValue() instanceof Number) {
            obj.setRequestedChannelMax(((Number)member.getValue()).intValue());
          }
          break;
        case "requestedHeartbeat":
          if (member.getValue() instanceof Number) {
            obj.setRequestedHeartbeat(((Number)member.getValue()).intValue());
          }
          break;
        case "reuseAddress":
          if (member.getValue() instanceof Boolean) {
            obj.setReuseAddress((Boolean)member.getValue());
          }
          break;
        case "reusePort":
          if (member.getValue() instanceof Boolean) {
            obj.setReusePort((Boolean)member.getValue());
          }
          break;
        case "sendBufferSize":
          if (member.getValue() instanceof Number) {
            obj.setSendBufferSize(((Number)member.getValue()).intValue());
          }
          break;
        case "soLinger":
          if (member.getValue() instanceof Number) {
            obj.setSoLinger(((Number)member.getValue()).intValue());
          }
          break;
        case "ssl":
          if (member.getValue() instanceof Boolean) {
            obj.setSsl((Boolean)member.getValue());
          }
          break;
        case "sslHandshakeTimeout":
          if (member.getValue() instanceof Number) {
            obj.setSslHandshakeTimeout(((Number)member.getValue()).longValue());
          }
          break;
        case "sslHandshakeTimeoutUnit":
          if (member.getValue() instanceof String) {
            obj.setSslHandshakeTimeoutUnit(java.util.concurrent.TimeUnit.valueOf((String)member.getValue()));
          }
          break;
        case "tcpCork":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpCork((Boolean)member.getValue());
          }
          break;
        case "tcpFastOpen":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpFastOpen((Boolean)member.getValue());
          }
          break;
        case "tcpKeepAlive":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpKeepAlive((Boolean)member.getValue());
          }
          break;
        case "tcpKeepAliveCount":
          if (member.getValue() instanceof Number) {
            obj.setTcpKeepAliveCount(((Number)member.getValue()).intValue());
          }
          break;
        case "tcpKeepAliveIdleSeconds":
          if (member.getValue() instanceof Number) {
            obj.setTcpKeepAliveIdleSeconds(((Number)member.getValue()).intValue());
          }
          break;
        case "tcpKeepAliveIntervalSeconds":
          if (member.getValue() instanceof Number) {
            obj.setTcpKeepAliveIntervalSeconds(((Number)member.getValue()).intValue());
          }
          break;
        case "tcpNoDelay":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpNoDelay((Boolean)member.getValue());
          }
          break;
        case "tcpQuickAck":
          if (member.getValue() instanceof Boolean) {
            obj.setTcpQuickAck((Boolean)member.getValue());
          }
          break;
        case "tcpUserTimeout":
          if (member.getValue() instanceof Number) {
            obj.setTcpUserTimeout(((Number)member.getValue()).intValue());
          }
          break;
        case "trafficClass":
          if (member.getValue() instanceof Number) {
            obj.setTrafficClass(((Number)member.getValue()).intValue());
          }
          break;
        case "trustAll":
          if (member.getValue() instanceof Boolean) {
            obj.setTrustAll((Boolean)member.getValue());
          }
          break;
        case "trustStoreOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setTrustStoreOptions(new io.vertx.core.net.JksOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "uri":
          if (member.getValue() instanceof String) {
            obj.setUri((String)member.getValue());
          }
          break;
        case "useAlpn":
          if (member.getValue() instanceof Boolean) {
            obj.setUseAlpn((Boolean)member.getValue());
          }
          break;
        case "useNio":
          if (member.getValue() instanceof Boolean) {
            obj.setUseNio((Boolean)member.getValue());
          }
          break;
        case "user":
          if (member.getValue() instanceof String) {
            obj.setUser((String)member.getValue());
          }
          break;
        case "virtualHost":
          if (member.getValue() instanceof String) {
            obj.setVirtualHost((String)member.getValue());
          }
          break;
        case "writeIdleTimeout":
          if (member.getValue() instanceof Number) {
            obj.setWriteIdleTimeout(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

   static void toJson(RabbitMQOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(RabbitMQOptions obj, java.util.Map<String, Object> json) {
    if (obj.getActivityLogDataFormat() != null) {
      json.put("activityLogDataFormat", obj.getActivityLogDataFormat().name());
    }
    if (obj.getApplicationLayerProtocols() != null) {
      JsonArray array = new JsonArray();
      obj.getApplicationLayerProtocols().forEach(item -> array.add(item));
      json.put("applicationLayerProtocols", array);
    }
    json.put("automaticRecoveryEnabled", obj.isAutomaticRecoveryEnabled());
    json.put("automaticRecoveryOnInitialConnection", obj.isAutomaticRecoveryOnInitialConnection());
    json.put("connectTimeout", obj.getConnectTimeout());
    if (obj.getConnectionName() != null) {
      json.put("connectionName", obj.getConnectionName());
    }
    json.put("connectionTimeout", obj.getConnectionTimeout());
    if (obj.getCrlPaths() != null) {
      JsonArray array = new JsonArray();
      obj.getCrlPaths().forEach(item -> array.add(item));
      json.put("crlPaths", array);
    }
    if (obj.getCrlValues() != null) {
      JsonArray array = new JsonArray();
      obj.getCrlValues().forEach(item -> array.add(BASE64_ENCODER.encodeToString(item.getBytes())));
      json.put("crlValues", array);
    }
    if (obj.getEnabledCipherSuites() != null) {
      JsonArray array = new JsonArray();
      obj.getEnabledCipherSuites().forEach(item -> array.add(item));
      json.put("enabledCipherSuites", array);
    }
    if (obj.getEnabledSecureTransportProtocols() != null) {
      JsonArray array = new JsonArray();
      obj.getEnabledSecureTransportProtocols().forEach(item -> array.add(item));
      json.put("enabledSecureTransportProtocols", array);
    }
    json.put("handshakeTimeout", obj.getHandshakeTimeout());
    if (obj.getHost() != null) {
      json.put("host", obj.getHost());
    }
    if (obj.getHostnameVerificationAlgorithm() != null) {
      json.put("hostnameVerificationAlgorithm", obj.getHostnameVerificationAlgorithm());
    }
    json.put("idleTimeout", obj.getIdleTimeout());
    if (obj.getIdleTimeoutUnit() != null) {
      json.put("idleTimeoutUnit", obj.getIdleTimeoutUnit().name());
    }
    json.put("includeProperties", obj.getIncludeProperties());
    if (obj.getJdkSslEngineOptions() != null) {
      json.put("jdkSslEngineOptions", obj.getJdkSslEngineOptions().toJson());
    }
    if (obj.getKeyStoreOptions() != null) {
      json.put("keyStoreOptions", obj.getKeyStoreOptions().toJson());
    }
    if (obj.getLocalAddress() != null) {
      json.put("localAddress", obj.getLocalAddress());
    }
    json.put("logActivity", obj.getLogActivity());
    if (obj.getMetricsName() != null) {
      json.put("metricsName", obj.getMetricsName());
    }
    json.put("networkRecoveryInterval", obj.getNetworkRecoveryInterval());
    json.put("nioEnabled", obj.isNioEnabled());
    if (obj.getNonProxyHosts() != null) {
      JsonArray array = new JsonArray();
      obj.getNonProxyHosts().forEach(item -> array.add(item));
      json.put("nonProxyHosts", array);
    }
    if (obj.getOpenSslEngineOptions() != null) {
      json.put("openSslEngineOptions", obj.getOpenSslEngineOptions().toJson());
    }
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    if (obj.getPemKeyCertOptions() != null) {
      json.put("pemKeyCertOptions", obj.getPemKeyCertOptions().toJson());
    }
    if (obj.getPemTrustOptions() != null) {
      json.put("pemTrustOptions", obj.getPemTrustOptions().toJson());
    }
    if (obj.getPfxKeyCertOptions() != null) {
      json.put("pfxKeyCertOptions", obj.getPfxKeyCertOptions().toJson());
    }
    if (obj.getPfxTrustOptions() != null) {
      json.put("pfxTrustOptions", obj.getPfxTrustOptions().toJson());
    }
    json.put("port", obj.getPort());
    if (obj.getProxyOptions() != null) {
      json.put("proxyOptions", obj.getProxyOptions().toJson());
    }
    json.put("readIdleTimeout", obj.getReadIdleTimeout());
    json.put("receiveBufferSize", obj.getReceiveBufferSize());
    json.put("reconnectAttempts", obj.getReconnectAttempts());
    json.put("reconnectInterval", obj.getReconnectInterval());
    json.put("registerWriteHandler", obj.isRegisterWriteHandler());
    json.put("requestedChannelMax", obj.getRequestedChannelMax());
    json.put("requestedHeartbeat", obj.getRequestedHeartbeat());
    json.put("reuseAddress", obj.isReuseAddress());
    json.put("reusePort", obj.isReusePort());
    json.put("sendBufferSize", obj.getSendBufferSize());
    json.put("soLinger", obj.getSoLinger());
    json.put("ssl", obj.isSsl());
    json.put("sslHandshakeTimeout", obj.getSslHandshakeTimeout());
    if (obj.getSslHandshakeTimeoutUnit() != null) {
      json.put("sslHandshakeTimeoutUnit", obj.getSslHandshakeTimeoutUnit().name());
    }
    json.put("tcpCork", obj.isTcpCork());
    json.put("tcpFastOpen", obj.isTcpFastOpen());
    json.put("tcpKeepAlive", obj.isTcpKeepAlive());
    json.put("tcpKeepAliveCount", obj.getTcpKeepAliveCount());
    json.put("tcpKeepAliveIdleSeconds", obj.getTcpKeepAliveIdleSeconds());
    json.put("tcpKeepAliveIntervalSeconds", obj.getTcpKeepAliveIntervalSeconds());
    json.put("tcpNoDelay", obj.isTcpNoDelay());
    json.put("tcpQuickAck", obj.isTcpQuickAck());
    json.put("tcpUserTimeout", obj.getTcpUserTimeout());
    json.put("trafficClass", obj.getTrafficClass());
    json.put("trustAll", obj.isTrustAll());
    if (obj.getTrustStoreOptions() != null) {
      json.put("trustStoreOptions", obj.getTrustStoreOptions().toJson());
    }
    if (obj.getUri() != null) {
      json.put("uri", obj.getUri());
    }
    json.put("useAlpn", obj.isUseAlpn());
    if (obj.getUser() != null) {
      json.put("user", obj.getUser());
    }
    if (obj.getVirtualHost() != null) {
      json.put("virtualHost", obj.getVirtualHost());
    }
    json.put("writeIdleTimeout", obj.getWriteIdleTimeout());
  }
}
