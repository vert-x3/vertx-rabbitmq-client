package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

import java.util.Date;
import java.util.Map;

/**
 * Similar to {@link com.rabbitmq.client.BasicProperties}
 */
@VertxGen
public interface BasicProperties {

  /**
   * Retrieve the value in the contentType field.
   *
   * @return contentType field, or null if the field has not been set.
   */
  @CacheReturn
  String contentType();

  /**
   * Retrieve the value in the contentEncoding field.
   *
   * @return contentEncoding field, or null if the field has not been set.
   */
  @CacheReturn
  String contentEncoding();

  /**
   * Retrieve the table in the headers field as a map of fields names and
   * values.
   *
   * @return headers table, or null if the headers field has not been set.
   */
  @GenIgnore
  Map<String, Object> headers();

  /**
   * Retrieve the value in the deliveryMode field.
   *
   * @return deliveryMode field, or null if the field has not been set.
   */
  @CacheReturn
  Integer deliveryMode();

  /**
   * Retrieve the value in the priority field.
   *
   * @return priority field, or null if the field has not been set.
   */
  @CacheReturn
  Integer priority();

  /**
   * Retrieve the value in the correlationId field.
   *
   * @return correlationId field, or null if the field has not been set.
   */
  @CacheReturn
  String correlationId();

  /**
   * Retrieve the value in the replyTo field.
   *
   * @return replyTo field, or null if the field has not been set.
   */
  @CacheReturn
  String replyTo();

  /**
   * Retrieve the value in the expiration field.
   *
   * @return expiration field, or null if the field has not been set.
   */
  @CacheReturn
  String expiration();

  /**
   * Retrieve the value in the messageId field.
   *
   * @return messageId field, or null if the field has not been set.
   */
  @CacheReturn
  String messageId();

  /**
   * Retrieve the value in the timestamp field.
   *
   * @return timestamp field, or null if the field has not been set.
   */
  Long timestamp();

  /**
   * Retrieve the value in the type field.
   *
   * @return type field, or null if the field has not been set.
   */
  @CacheReturn
  String type();

  /**
   * Retrieve the value in the userId field.
   *
   * @return userId field, or null if the field has not been set.
   */
  @CacheReturn
  String userId();

  /**
   * Retrieve the value in the appId field.
   *
   * @return appId field, or null if the field has not been set.
   */
  @CacheReturn
  String appId();

  /**
   * Retrieve the value in the clusterId field.
   *
   * @return clusterId field, or null if the field has not been set.
   */
  @CacheReturn
  String clusterId();
}
