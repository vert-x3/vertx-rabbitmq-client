package io.vertx.rabbitmq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.TrustOptions;

/**
 * RabbitMQ client options, most
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true, inheritConverter = true)
public class RabbitMQOptions extends NetClientOptions {

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
   * The default reconnect on initial connection = {@code true}
   */
  public static final boolean DEFAULT_RECONNECT_ON_INITIAL_CONNECTION = true;

  /**
   * The default connection retry delay = {@code 10000}
   */
  public static final long DEFAULT_RECONNECT_INTERVAL = 10000L;

  /**
   * The default use nio sockets = {@code false}
   */
  public static final boolean DEFAULT_USE_NIO_SOCKETS = false;

  private String uri = null;
  private List<Address> addresses = Collections.emptyList();
  private String user;
  private String password;
  private String host;
  private String virtualHost;
  private int port;
  private int connectionTimeout;
  private int requestedHeartbeat;
  private int handshakeTimeout;
  private int requestedChannelMax;
  
// These two control the java RabbitMQ client automatic recovery
  private boolean automaticRecoveryEnabled;
  private long networkRecoveryInterval;
  
  // This (and reconnectAttempts, reconnectInterval from NetClientOptions) control the reconnects implented in this library
  private boolean reconnectOnInitialConnection;
  
  private boolean includeProperties;
  private boolean useNio;

  public RabbitMQOptions() {
    super();
    setReconnectInterval(DEFAULT_RECONNECT_INTERVAL);
    init();
  }

  public RabbitMQOptions(JsonObject json) {
    super(json);
    init();
    RabbitMQOptionsConverter.fromJson(json, this);
  }

  public RabbitMQOptions(RabbitMQOptions other) {
    super(other);
    this.uri = other.uri;
    this.addresses = other.addresses;
    this.user = other.user;
    this.password = other.password;
    this.host = other.host;
    this.virtualHost = other.virtualHost;
    this.port = other.port;
    this.connectionTimeout = other.connectionTimeout;
    this.requestedHeartbeat = other.requestedHeartbeat;
    this.handshakeTimeout = other.handshakeTimeout;
    this.networkRecoveryInterval = other.networkRecoveryInterval;
    this.automaticRecoveryEnabled = other.automaticRecoveryEnabled;
    this.reconnectOnInitialConnection = other.reconnectOnInitialConnection;
    this.includeProperties = other.includeProperties;
    this.requestedChannelMax = other.requestedChannelMax;
    this.useNio = other.useNio;
  }

  private void init() {
    this.uri = null;
    this.addresses = Collections.emptyList();
    this.user = DEFAULT_USER;
    this.password = DEFAULT_PASSWORD;
    this.host = DEFAULT_HOST;
    this.virtualHost = DEFAULT_VIRTUAL_HOST;
    this.port = DEFAULT_PORT;
    this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    this.requestedHeartbeat = DEFAULT_REQUESTED_HEARTBEAT;
    this.handshakeTimeout = DEFAULT_HANDSHAKE_TIMEOUT;
    this.requestedChannelMax = DEFAULT_REQUESTED_CHANNEL_MAX;
    this.networkRecoveryInterval = DEFAULT_NETWORK_RECOVERY_INTERNAL;
    this.automaticRecoveryEnabled = DEFAULT_AUTOMATIC_RECOVERY_ENABLED;
    this.reconnectOnInitialConnection = DEFAULT_RECONNECT_ON_INITIAL_CONNECTION;
    this.includeProperties = false;
    this.useNio = DEFAULT_USE_NIO_SOCKETS;
  }


  public List<Address> getAddresses() {
    return Collections.unmodifiableList(addresses);
  }

  /**
   * Set multiple addresses for cluster mode.
   *
   * @param addresses addresses of AMQP cluster
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setAddresses(List<Address> addresses) {
    this.addresses = new ArrayList<>(addresses);
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

  public boolean isReconnectOnInitialConnection() {
    return reconnectOnInitialConnection;
  }

  /**
   * Enable or disable reconnection, as implemented in this library, on initial connections.
   * 
   * If reconnections are enabled it will, by default, make multiple attempts to connect on startup.
   * This can cause problems with the configuration is wrong, and it is this bad configuration that is preventing connection.
   * To work around this reconnectOnInitialConnection can be set to false (it defaults to true).
   * When reconnectOnInitialConnection is false (and reconnectAttempts > 0) reconnection attempts will not be made until
   * after the first connection has been successful.
   * 
   * @param reconnectOnInitialConnection if {@code false}, prevents automatic recovery on the first connection attempts.
   * @return a reference to this, so the API can be used fluently
   * 
   */
  public RabbitMQOptions setReconnectOnInitialConnection(boolean reconnectOnInitialConnection) {
    this.reconnectOnInitialConnection = reconnectOnInitialConnection;
    return this;
  }
  
  /**
   * @return wether to include properties when a broker message is passed on the event bus
   */
  public boolean getIncludeProperties() {
    return includeProperties;
  }

  /**
   * Set wether to include properties when a broker message is passed on the event bus
   *
   * @param includeProperties wether to include properties
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setIncludeProperties(boolean includeProperties) {
    this.includeProperties = includeProperties;
    return this;
  }

  /**
   * @return {@code true} if NIO Sockets are enabled, {@code false} otherwise
   */
  public boolean isNioEnabled() {
    return useNio;
  }

  /**
   * Enables or disables usage of NIO Sockets.
   *
   * @param useNio if {@code true}, enables NIO Sockets
   * @return a reference to this, so the API can be used fluently
   */
  public RabbitMQOptions setUseNio(boolean useNio) {
    this.useNio = useNio;
    return this;
  }

  @Override
  public RabbitMQOptions setReconnectAttempts(int attempts) {
    super.setReconnectAttempts(attempts);
    return this;
  }

  @Override
  public RabbitMQOptions setReconnectInterval(long interval) {
    super.setReconnectInterval(interval);
    return this;
  }

  @Override
  public RabbitMQOptions setSsl(boolean ssl) {
    super.setSsl(ssl);
    return this;
  }

  @Override
  public RabbitMQOptions setTrustAll(boolean trustAll) {
    super.setTrustAll(trustAll);
    return this;
  }

  @Override
  public RabbitMQOptions setKeyCertOptions(KeyCertOptions options) {
    super.setKeyCertOptions(options);
    return this;
  }

  @Override
  public RabbitMQOptions setKeyStoreOptions(JksOptions options) {
    super.setKeyStoreOptions(options);
    return this;
  }

  @Override
  public RabbitMQOptions setPfxKeyCertOptions(PfxOptions options) {
    super.setPfxKeyCertOptions(options);
    return this;
  }

  @Override
  public RabbitMQOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    super.setPemKeyCertOptions(options);
    return this;
  }

  @Override
  public RabbitMQOptions setTrustOptions(TrustOptions options) {
    super.setTrustOptions(options);
    return this;
  }

  @Override
  public RabbitMQOptions setPemTrustOptions(PemTrustOptions options) {
    super.setPemTrustOptions(options);
    return this;
  }

  @Override
  public RabbitMQOptions setPfxTrustOptions(PfxOptions options) {
    super.setPfxTrustOptions(options);
    return this;
  }


}
