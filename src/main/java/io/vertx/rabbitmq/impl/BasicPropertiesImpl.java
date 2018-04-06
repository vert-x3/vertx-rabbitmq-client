package io.vertx.rabbitmq.impl;

import io.vertx.rabbitmq.BasicProperties;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BasicPropertiesImpl implements BasicProperties {

  private String contentType;
  private String contentEncoding;
  private Map<String,Object> headers;
  private Integer deliveryMode;
  private Integer priority;
  private String correlationId;
  private String replyTo;
  private String expiration;
  private String messageId;
  private Date timestamp;
  private String type;
  private String userId;
  private String appId;
  private String clusterId;

  BasicPropertiesImpl(String contentType,
                      String contentEncoding,
                      Map<String, Object> headers,
                      Integer deliveryMode,
                      Integer priority,
                      String correlationId,
                      String replyTo,
                      String expiration,
                      String messageId,
                      Date timestamp,
                      String type,
                      String userId,
                      String appId,
                      String clusterId) {
    this.contentType = contentType;
    this.contentEncoding = contentEncoding;
    this.headers = headers == null ? null : Collections.unmodifiableMap(new HashMap<>(headers));
    this.deliveryMode = deliveryMode;
    this.priority = priority;
    this.correlationId = correlationId;
    this.replyTo = replyTo;
    this.expiration = expiration;
    this.messageId = messageId;
    this.timestamp = timestamp;
    this.type = type;
    this.userId = userId;
    this.appId = appId;
    this.clusterId = clusterId;
  }

  @Override
  public String contentType() {
    return contentType;
  }

  @Override
  public String contentEncoding() {
    return contentEncoding;
  }

  @Override
  public Map<String, Object> headers() {
    return headers;
  }

  @Override
  public Integer deliveryMode() {
    return deliveryMode;
  }

  @Override
  public Integer priority() {
    return priority;
  }

  @Override
  public String correlationId() {
    return correlationId;
  }

  @Override
  public String replyTo() {
    return replyTo;
  }

  @Override
  public String expiration() {
    return expiration;
  }

  @Override
  public String messageId() {
    return messageId;
  }

  @Override
  public Long timestamp() {
    return timestamp.toInstant().toEpochMilli();
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public String userId() {
    return userId;
  }

  @Override
  public String appId() {
    return appId;
  }

  @Override
  public String clusterId() {
    return clusterId;
  }
}
