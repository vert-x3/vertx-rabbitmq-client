package io.vertx.rabbitmq;

import javax.net.ssl.SSLContext;

import com.rabbitmq.client.ConnectionFactory;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * RabbitMQ client options, most
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class RabbitMQOptions {

  /**
   * The default port = {@code - 1} - {@code 5671} for SSL otherwise {@code 5672}
   */
  public static final int DEFAULT_PORT = -1;

  /**
   * The default host = {@code localhost}
   */
  public static final String DEFAULT_HOST = ConnectionFactory.DEFAULT_HOST;

  /**
   * The default user = {@code guest}
   */
  public static final String DEFAULT_USER = ConnectionFactory.DEFAULT_USER;

  /**
   * The default password = {@code guest}
   */
  public static final String DEFAULT_PASSWORD = ConnectionFactory.DEFAULT_PASS;

  /**
   * The default virtual host = {@code /}
   */
  public static final String DEFAULT_VIRTUAL_HOST = ConnectionFactory.DEFAULT_VHOST;

  /**
   * The default connection timeout = {@code 60000}
   */
  public static final int DEFAULT_CONNECTION_TIMEOUT = ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT;

  /**
   * The default connection timeout = {@code 60}
   */
  public static final int DEFAULT_REQUESTED_HEARTBEAT = ConnectionFactory.DEFAULT_HEARTBEAT;

  /**
   * The default handshake timeout = {@code 10000}
   */
  public static final int DEFAULT_HANDSHAKE_TIMEOUT = ConnectionFactory.DEFAULT_HANDSHAKE_TIMEOUT;

  /**
   * The default requested channel max = {@code 0}
   */
  public static final int DEFAULT_REQUESTED_CHANNEL_MAX = ConnectionFactory.DEFAULT_CHANNEL_MAX;

  /**
   * The default network recovery internal = {@code 5000}
   */
  public static final long DEFAULT_NETWORK_RECOVERY_INTERNAL = 5000L;

  /**
   * The default automatic recovery enabled = {@code false}
   */
  public static final boolean DEFAULT_AUTOMATIC_RECOVERY_ENABLED = false;

  /**
   * The default connection retry delay = {@code 10000}
   */
  public static final long DEFAULT_CONNECTION_RETRY_DELAY = 10000L;

  /**
   * The default connection retries = {@code null} (no retry)
   */
  public static final Integer DEFAULT_CONNECTION_RETRIES = null;
  
  /**
   * The default value for encrypting the connection through SSL or not = {@code false}
   */
  public static final boolean DEFAULT_USE_SSL = false;
  
  /**
   * The default value for which specific SSLContext to use = {@code null}
   */
  public static final SSLContext DEFAULT_SSL_CONTEXT = null;

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
  private boolean useSsl = DEFAULT_USE_SSL;
  private SSLContext sslContext = DEFAULT_SSL_CONTEXT;

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
    useSsl = that.useSsl;
    sslContext = that.sslContext;
  }

  /**
   * @return the number of connection retries
   */
  public Integer getConnectionRetries() {
    return connectionRetries;
  }

  /**
   * Set the number of connection retries to attempt when connecting, the {@code null} value disables it.
   *
   * @param connectionRetries the number of retries
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setConnectionRetries(Integer connectionRetries) {
    this.connectionRetries = connectionRetries;
    return this;
  }

  /**
   * @return the delay in milliseconds between connection retries
   */
  public long getConnectionRetryDelay() {
    return connectionRetryDelay;
  }

  /**
   * Set the delay in milliseconds between connection retries.
   *
   * @param connectionRetryDelay the delay in milliseconds
   * @return a reference to this, so the API can be used fluently
   */
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

  /**
   * @return the AMQP user name to use when connecting to the broker
   */
  public String getUser() {
    return user;
  }

  /**
   * Set the AMQP user name to use when connecting to the broker.
   *
   * @param user the user name
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setUser(String user) {
    this.user = user;
    return this;
  }

  /**
   * @return the password to use when connecting to the broker
   */
  public String getPassword() {
    return password;
  }

  /**
   * Set the password to use when connecting to the broker.
   *
   * @param password the password
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setPassword(String password) {
    this.password = password;
    return this;
  }

  /**
   * @return the default host to use for connections
   */
  public String getHost() {
    return host;
  }

  /**
   * Set the default host to use for connections.
   *
   * @param host the default host
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setHost(String host) {
    this.host = host;
    return this;
  }

  /**
   * @return the virtual host to use when connecting to the broker
   */
  public String getVirtualHost() {
    return virtualHost;
  }

  /**
   * Set the virtual host to use when connecting to the broker.
   *
   * @param virtualHost the virtual host
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
    return this;
  }

  /**
   * @return the default port to use for connections
   */
  public int getPort() {
    return port;
  }

  /**
   * Set the default port to use for connections.
   *
   * @param port the default port
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * @return the TCP connection timeout
   */
  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Set the TCP connection timeout, in milliseconds, {@code zero} for infinite).
   *
   * @param connectionTimeout the timeouut in milliseconds.
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
    return this;
  }

  /**
   * @return the initially requested heartbeat interval
   */
  public int getRequestedHeartbeat() {
    return requestedHeartbeat;
  }

  /**
   * Set the initially requested heartbeat interval, in seconds, {@code zero} for none.
   *
   * @param requestedHeartbeat the request heartbeat interval
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setRequestedHeartbeat(int requestedHeartbeat) {
    this.requestedHeartbeat = requestedHeartbeat;
    return this;
  }

  /**
   * @return the AMQP 0-9-1 protocol handshake timeout
   */
  public int getHandshakeTimeout() {
    return handshakeTimeout;
  }

  /**
   * Set the AMQP 0-9-1 protocol handshake timeout, in milliseconds
   *
   * @param handshakeTimeout the timeout in milliseconds
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setHandshakeTimeout(int handshakeTimeout) {
    this.handshakeTimeout = handshakeTimeout;
    return this;
  }

  /**
   * @return the initially requested maximum channel number
   */
  public int getRequestedChannelMax() {
    return requestedChannelMax;
  }

  /**
   * Set the initially requested maximum channel number, {@code zero} for unlimited.
   *
   * @param requestedChannelMax the requested maximum channel number
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setRequestedChannelMax(int requestedChannelMax) {
    this.requestedChannelMax = requestedChannelMax;
    return this;
  }

  /**
   * @return automatic connection recovery interval
   */
  public long getNetworkRecoveryInterval() {
    return networkRecoveryInterval;
  }

  /**
   * Set how long in milliseconds will automatic recovery wait before attempting to reconnect, default is {@code 5000}
   *
   * @param networkRecoveryInterval the connection recovery interval
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setNetworkRecoveryInterval(long networkRecoveryInterval) {
    this.networkRecoveryInterval = networkRecoveryInterval;
    return this;
  }

  /**
   * @return {@code true} if automatic connection recovery is enabled, {@code false} otherwise
   */
  public boolean isAutomaticRecoveryEnabled() {
    return automaticRecoveryEnabled;
  }

  /**
   * Enables or disables automatic connection recovery.
   *
   * @param automaticRecoveryEnabled if {@code true}, enables connection recovery
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) {
    this.automaticRecoveryEnabled = automaticRecoveryEnabled;
    return this;
  }

  /**
   * @return whether to include properties when a broker message is passed on the event bus
   */
  public boolean getIncludeProperties() {
    return includeProperties;
  }

  /**
   * Set whether to include properties when a broker message is passed on the event bus
   *
   * @param includeProperties whether to include properties
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setIncludeProperties(boolean includeProperties) {
    this.includeProperties = includeProperties;
    return this;
  }
  
  /**
   * Set whether to use SSL or not
   * @param useSsl if {@code true}, enables SSL
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setSsl(boolean useSsl) {
	  this.useSsl = useSsl;
	  return this;
  }
  /**
   * @return whether the connection use SSL encryption or not
   */
  public boolean getSsl() {
	  return this.useSsl;
  }
  
  /**
   * Set which SSLContext to use. Also enables the SSL flag for this connection.
   * @param sslContext SSLContext to use
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setSslContext(SSLContext sslContext) {
	  this.sslContext = sslContext;
	  this.useSsl = true;
	  return this;
  }
  
  public SSLContext getSslContext() {
	  return this.sslContext;
  }
}
