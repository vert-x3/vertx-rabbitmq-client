package io.vertx.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class RabbitMQOptions {

  /**
   * The default port = {@literal - 1} - {@literal 5671} for SSL otherwise {@literal 5672}
   */
  public static final int DEFAULT_PORT = -1;

  /**
   * The default host = {@literal localhost}
   */
  public static final String DEFAULT_HOST = ConnectionFactory.DEFAULT_HOST;

  /**
   * The default user = {@literal guest}
   */
  public static final String DEFAULT_USER = ConnectionFactory.DEFAULT_USER;

  /**
   * The default password = {@literal guest}
   */
  public static final String DEFAULT_PASSWORD = ConnectionFactory.DEFAULT_PASS;

  /**
   * The default virtual host = {@literal /}
   */
  public static final String DEFAULT_VIRTUAL_HOST = ConnectionFactory.DEFAULT_VHOST;

  /**
   * The default connection timeout = {@literal 60000}
   */
  public static final int DEFAULT_CONNECTION_TIMEOUT = ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT;

  /**
   * The default connection timeout = {@literal 60}
   */
  public static final int DEFAULT_REQUESTED_HEARTBEAT = ConnectionFactory.DEFAULT_HEARTBEAT;

  /**
   * The default handshake timeout = {@literal 10000}
   */
  public static final int DEFAULT_HANDSHAKE_TIMEOUT = ConnectionFactory.DEFAULT_HANDSHAKE_TIMEOUT;

  /**
   * The default requested channel max = {@literal 0}
   */
  public static final int DEFAULT_REQUESTED_CHANNEL_MAX = ConnectionFactory.DEFAULT_CHANNEL_MAX;

  /**
   * The default network recovery internal = {@literal 5000}
   */
  public static final long DEFAULT_NETWORK_RECOVERY_INTERNAL = 5000L;

  /**
   * The default automatic recovery enabled = {@literal false}
   */
  public static final boolean DEFAULT_AUTOMATIC_RECOVERY_ENABLED = false;

  /**
   * The default connection retry delay = {@literal 10000}
   */
  public static final long DEFAULT_CONNECTION_RETRY_DELAY = 10000L;

  /**
   * The default connection retries = {@literal null} (no retry)
   */
  public static final Integer DEFAULT_CONNECTION_RETRIES = null;

  private Integer connectionRetries = DEFAULT_CONNECTION_RETRIES;
  private long connectionRetryDelay = DEFAULT_CONNECTION_RETRY_DELAY;
  private String uri = null;
  private String user = DEFAULT_USER;
  private String password = DEFAULT_PASSWORD;
  private String host = DEFAULT_HOST;
  private String virtualHost = DEFAULT_VIRTUAL_HOST;
  private int port = DEFAULT_PORT;
  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
  private int requestedHeartbeat = DEFAULT_REQUESTED_HEARTBEAT;
  private int handshakeTimeout = DEFAULT_HANDSHAKE_TIMEOUT;
  private int requestedChannelMax = DEFAULT_REQUESTED_CHANNEL_MAX;
  private long networkRecoveryInterval = DEFAULT_NETWORK_RECOVERY_INTERNAL;
  private boolean automaticRecoveryEnabled = DEFAULT_AUTOMATIC_RECOVERY_ENABLED;
  private boolean includeProperties = false;

  public RabbitMQOptions() {
  }

  public RabbitMQOptions(JsonObject json) {
    this();
    RabbitMQOptionsConverter.fromJson(json, this);
  }

  public RabbitMQOptions(RabbitMQOptions that) {
    connectionRetries = that.connectionRetries;
    connectionRetryDelay = that.connectionRetryDelay;
    uri = that.uri;
    user = that.user;
    password = that.password;
    host = that.host;
    virtualHost = that.virtualHost;
    port = that.port;
    connectionTimeout = that.connectionTimeout;
    requestedHeartbeat = that.requestedHeartbeat;
    handshakeTimeout = that.handshakeTimeout;
    networkRecoveryInterval = that.networkRecoveryInterval;
    automaticRecoveryEnabled = that.automaticRecoveryEnabled;
    includeProperties = that.includeProperties;
    requestedChannelMax = that.requestedChannelMax;
  }

  public Integer getConnectionRetries() {
    return connectionRetries;
  }

  public RabbitMQOptions setConnectionRetries(Integer connectionRetries) {
    this.connectionRetries = connectionRetries;
    return this;
  }

  public long getConnectionRetryDelay() {
    return connectionRetryDelay;
  }

  public RabbitMQOptions setConnectionRetryDelay(long connectionRetryDelay) {
    this.connectionRetryDelay = connectionRetryDelay;
    return this;
  }

  public String getUri() {
    return uri;
  }

  public RabbitMQOptions setUri(String uri) {
    this.uri = uri;
    return this;
  }

  public String getUser() {
    return user;
  }

  public RabbitMQOptions setUser(String user) {
    this.user = user;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public RabbitMQOptions setPassword(String password) {
    this.password = password;
    return this;
  }

  public String getHost() {
    return host;
  }

  public RabbitMQOptions setHost(String host) {
    this.host = host;
    return this;
  }

  public String getVirtualHost() {
    return virtualHost;
  }

  public RabbitMQOptions setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
    return this;
  }

  public int getPort() {
    return port;
  }

  public RabbitMQOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public RabbitMQOptions setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
    return this;
  }

  public int getRequestedHeartbeat() {
    return requestedHeartbeat;
  }

  public RabbitMQOptions setRequestedHeartbeat(int requestedHeartbeat) {
    this.requestedHeartbeat = requestedHeartbeat;
    return this;
  }

  public int getHandshakeTimeout() {
    return handshakeTimeout;
  }

  public RabbitMQOptions setHandshakeTimeout(int handshakeTimeout) {
    this.handshakeTimeout = handshakeTimeout;
    return this;
  }

  public int getRequestedChannelMax() {
    return requestedChannelMax;
  }

  public RabbitMQOptions setRequestedChannelMax(int requestedChannelMax) {
    this.requestedChannelMax = requestedChannelMax;
    return this;
  }

  public long getNetworkRecoveryInterval() {
    return networkRecoveryInterval;
  }

  public RabbitMQOptions setNetworkRecoveryInterval(long networkRecoveryInterval) {
    this.networkRecoveryInterval = networkRecoveryInterval;
    return this;
  }

  public boolean isAutomaticRecoveryEnabled() {
    return automaticRecoveryEnabled;
  }

  public RabbitMQOptions setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) {
    this.automaticRecoveryEnabled = automaticRecoveryEnabled;
    return this;
  }

  public boolean getIncludeProperties() {
    return includeProperties;
  }

  public RabbitMQOptions setIncludeProperties(boolean includeProperties) {
    this.includeProperties = includeProperties;
    return this;
  }
}
