package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

import java.util.Map;

/**
 * Like {@link com.rabbitmq.client.BasicProperties}
 */
@VertxGen
public interface BasicProperties {

  /**
   * @return contentType of a message, or {@code null} if it has not been set.
   */
  @CacheReturn
  String contentType();

  /**
   * @return contentEncoding of a message, or {@code null} if it has not been set.
   */
  @CacheReturn
  String contentEncoding();

  /**
   * @return headers table related to a message, or {@code null} if it has not been set.
   */
  @GenIgnore
  Map<String, Object> headers();

  /**
   * @return deliveryMode of a message, or {@code null} if it has not been set.
   */
  @CacheReturn
  Integer deliveryMode();

  /**
   * Retrieve the value in the priority field.
   *
   * @return priority of a message, or {@code null} if it has not been set.
   */
  @CacheReturn
  Integer priority();

  /**
   * @return correlationId of a message, or {@code null} if it has not been set.
   */
  @CacheReturn
  String correlationId();

  /**
   * @return replyTo address, or {@code null} if it has not been set.
   */
  @CacheReturn
  String replyTo();

  /**
   * @return expiration of a message, or {@code null} if it has not been set.
   */
  @CacheReturn
  String expiration();

  /**
   * @return messageId, or {@code null} if it has not been set.
   */
  @CacheReturn
  String messageId();

  /**
   * @return timestamp of a message, or {@code null} if it has not been set.
   */
  @CacheReturn
  Long timestamp();

  /**
   * @return type of a message, or {@code null} if it has not been set.
   */
  @CacheReturn
  String type();

  /**
   * @return userId, or {@code null} if it has not been set.
   */
  @CacheReturn
  String userId();

  /**
   * @return appId, or {@code null} if it has not been set.
   */
  @CacheReturn
  String appId();

  /**
   * @return clusterId, or {@code null} if it has not been set.
   */
  @CacheReturn
  String clusterId();
}
