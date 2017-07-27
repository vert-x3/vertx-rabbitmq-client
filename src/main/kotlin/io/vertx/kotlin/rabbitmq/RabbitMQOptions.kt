package io.vertx.kotlin.rabbitmq

import io.vertx.rabbitmq.RabbitMQOptions

/**
 * A function providing a DSL for building [io.vertx.rabbitmq.RabbitMQOptions] objects.
 *
 *
 * @param automaticRecoveryEnabled 
 * @param connectionRetries 
 * @param connectionRetryDelay 
 * @param connectionTimeout 
 * @param handshakeTimeout 
 * @param host 
 * @param includeProperties 
 * @param networkRecoveryInterval 
 * @param password 
 * @param port 
 * @param requestedChannelMax 
 * @param requestedHeartbeat 
 * @param uri 
 * @param user 
 * @param virtualHost 
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.rabbitmq.RabbitMQOptions original] using Vert.x codegen.
 */
fun RabbitMQOptions(
  automaticRecoveryEnabled: Boolean? = null,
  connectionRetries: Int? = null,
  connectionRetryDelay: Long? = null,
  connectionTimeout: Int? = null,
  handshakeTimeout: Int? = null,
  host: String? = null,
  includeProperties: Boolean? = null,
  networkRecoveryInterval: Long? = null,
  password: String? = null,
  port: Int? = null,
  requestedChannelMax: Int? = null,
  requestedHeartbeat: Int? = null,
  uri: String? = null,
  user: String? = null,
  virtualHost: String? = null): RabbitMQOptions = io.vertx.rabbitmq.RabbitMQOptions().apply {

  if (automaticRecoveryEnabled != null) {
    this.setAutomaticRecoveryEnabled(automaticRecoveryEnabled)
  }
  if (connectionRetries != null) {
    this.setConnectionRetries(connectionRetries)
  }
  if (connectionRetryDelay != null) {
    this.setConnectionRetryDelay(connectionRetryDelay)
  }
  if (connectionTimeout != null) {
    this.setConnectionTimeout(connectionTimeout)
  }
  if (handshakeTimeout != null) {
    this.setHandshakeTimeout(handshakeTimeout)
  }
  if (host != null) {
    this.setHost(host)
  }
  if (includeProperties != null) {
    this.setIncludeProperties(includeProperties)
  }
  if (networkRecoveryInterval != null) {
    this.setNetworkRecoveryInterval(networkRecoveryInterval)
  }
  if (password != null) {
    this.setPassword(password)
  }
  if (port != null) {
    this.setPort(port)
  }
  if (requestedChannelMax != null) {
    this.setRequestedChannelMax(requestedChannelMax)
  }
  if (requestedHeartbeat != null) {
    this.setRequestedHeartbeat(requestedHeartbeat)
  }
  if (uri != null) {
    this.setUri(uri)
  }
  if (user != null) {
    this.setUser(user)
  }
  if (virtualHost != null) {
    this.setVirtualHost(virtualHost)
  }
}

