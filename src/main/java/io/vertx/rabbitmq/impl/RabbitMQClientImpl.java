package io.vertx.rabbitmq.impl;

import static io.vertx.rabbitmq.impl.Utils.encode;
import static io.vertx.rabbitmq.impl.Utils.fromJson;
import static io.vertx.rabbitmq.impl.Utils.parse;
import static io.vertx.rabbitmq.impl.Utils.populate;
import static io.vertx.rabbitmq.impl.Utils.put;
import static io.vertx.rabbitmq.impl.Utils.toJson;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQClientImpl implements RabbitMQClient, ShutdownListener {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQClientImpl.class);
  private static final JsonObject emptyConfig = new JsonObject();

  private final Vertx vertx;
  private final RabbitMQOptions config;
  private final Integer retries;
  private final boolean includeProperties;

  private Connection connection;
  private Channel channel;
  private boolean channelConfirms = false;

  public RabbitMQClientImpl(Vertx vertx, RabbitMQOptions config) {
    this.vertx = vertx;
    this.config = config;
    this.retries = config.getConnectionRetries();
    //TODO: includeProperties isn't really intuitive
    //TODO: Think about allowing this at a method level ?
    this.includeProperties = config.getIncludeProperties();
  }

  private static Connection newConnection(RabbitMQOptions config) throws IOException, TimeoutException {
    ConnectionFactory cf = new ConnectionFactory();
    String uri = config.getUri();
    // Use uri if set, otherwise support individual connection parameters
    if (uri != null) {
      try {
        cf.setUri(uri);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid rabbitmq connection uri " + uri);
      }
    } else {
      cf.setUsername(config.getUser());
      cf.setPassword(config.getPassword());
      cf.setHost(config.getHost());
      cf.setPort(config.getPort());
      cf.setVirtualHost(config.getVirtualHost());
    }

    cf.setConnectionTimeout(config.getConnectionTimeout());
    cf.setRequestedHeartbeat(config.getRequestedHeartbeat());
    cf.setHandshakeTimeout(config.getHandshakeTimeout());
    cf.setRequestedChannelMax(config.getRequestedChannelMax());
    cf.setNetworkRecoveryInterval(config.getNetworkRecoveryInterval());
    cf.setAutomaticRecoveryEnabled(config.isAutomaticRecoveryEnabled());


    //TODO: Support other configurations

    return cf.newConnection();
  }

  @Override
  public boolean isConnected() {
    boolean connected = false;
    if (connection != null) {
      if (connection.isOpen()) {
        connected = true;
      }
    }
    return connected;
  }

  @Override
  public boolean isOpenChannel() {
    return channel != null && channel.isOpen();
  }

  @Override
  public void basicAck(long deliveryTag, boolean multiple, Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, (channel) -> {
      channel.basicAck(deliveryTag, multiple);
      return null;
    });
  }

  @Override
  public void basicNack(long deliveryTag, boolean multiple, boolean requeue, Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, (channel) -> {
      channel.basicNack(deliveryTag, multiple, requeue);
      return null;
    });
  }

  @Override
  public void basicConsumer(String queue, QueueOptions options, Handler<AsyncResult<RabbitMQConsumer>> resultHandler) {
    forChannel(resultHandler, channel -> {
      QueueConsumerHandler handler = new QueueConsumerHandler(vertx, channel, includeProperties, options);
      String consumerTag = channel.basicConsume(queue, options.isAutoAck(), handler);
      return handler.queue();
    });
  }

  @Override
  public void basicConsume(String queue, String address, Handler<AsyncResult<Void>> resultHandler) {
    basicConsume(queue, address, true, resultHandler);
  }

  @Override
  public void basicConsume(String queue, String address, boolean autoAck, Handler<AsyncResult<Void>> resultHandler) {
    basicConsume(queue, address, autoAck, resultHandler, null);
  }

  @Override
  public void basicConsume(String queue, String address, boolean autoAck, Handler<AsyncResult<Void>> resultHandler, Handler<Throwable> errorHandler) {
    forChannel(resultHandler, channel -> {
      channel.basicConsume(queue, autoAck, new ConsumerHandler(vertx, channel, includeProperties, ar -> {
        if (ar.succeeded()) {
          vertx.eventBus().send(address, ar.result());
        } else {
          log.error("Exception occurred inside rabbitmq service consumer.", ar.cause());
          if (errorHandler != null) {
            errorHandler.handle(ar.cause());
          }
        }
      }));
      return null;
    });
  }

  @Override
  public void basicGet(String queue, boolean autoAck, Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, (channel) -> {
      GetResponse response = channel.basicGet(queue, autoAck);
      if (response == null) {
        return null;
      } else {
        JsonObject json = new JsonObject();
        populate(json, response.getEnvelope());
        if (includeProperties) {
          put("properties", Utils.toJson(response.getProps()), json);
        }
        put("body", parse(response.getProps(), response.getBody()), json);
        Utils.put("messageCount", response.getMessageCount(), json);
        return json;
      }
    });
  }

  @Override
  public void basicPublish(String exchange, String routingKey, JsonObject message, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      //TODO: Really need an SPI / Interface to decouple this and allow pluggable implementations
      JsonObject properties = message.getJsonObject("properties");
      String contentType = properties == null ? null : properties.getString("contentType");
      String encoding = properties == null ? null : properties.getString("contentEncoding");
      byte[] body;
      if (contentType != null) {
        switch (contentType) {
          case "application/json":
            body = encode(encoding, message.getJsonObject("body").toString());
            break;
          case "application/octet-stream":
            body = message.getBinary("body");
            break;
          case "text/plain":
          default:
            body = encode(encoding, message.getString("body"));
        }
      } else {
        body = encode(encoding, message.getString("body"));
      }

      channel.basicPublish(exchange, routingKey, fromJson(properties), body);
      return null;
    });
  }

  @Override
  public void confirmSelect(Handler<AsyncResult<Void>> resultHandler) {
    forChannel(  resultHandler, channel -> {

      channel.confirmSelect();

      channelConfirms = true;

      return null;
    });
  }

  @Override
  public void waitForConfirms(Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.waitForConfirmsOrDie();

      return null;
    });
  }

  @Override
  public void waitForConfirms(long timeout, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.waitForConfirmsOrDie(timeout);

      return null;
    });
  }

  @Override
  public void basicQos(int prefetchSize, int prefetchCount, boolean global, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.basicQos(prefetchSize, prefetchCount, global);
      return null;
    });
  }

  @Override
  public void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler) {
    exchangeDeclare(exchange, type, durable, autoDelete, emptyConfig, resultHandler);
  }

  @Deprecated
  @Override
  public void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Map<String, String> config,
                              Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.exchangeDeclare(exchange, type, durable, autoDelete, toArgumentsMap(config));
      return null;
    });
  }

  @Override
  public void exchangeDeclare(
    String exchange,
    String type,
    boolean durable,
    boolean autoDelete,
    JsonObject config,
    Handler<AsyncResult<Void>> resultHandler
  ) {
    forChannel(resultHandler, channel -> {
      channel.exchangeDeclare(exchange, type, durable, autoDelete, new LinkedHashMap<>(config.getMap()));
      return null;
    });
  }

  @Override
  public void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.exchangeDelete(exchange);
      return null;
    });
  }

  @Override
  public void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.exchangeBind(destination, source, routingKey);
      return null;
    });
  }

  @Override
  public void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.exchangeUnbind(destination, source, routingKey);
      return null;
    });
  }

  @Override
  public void queueDeclareAuto(Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, channel -> {
      AMQP.Queue.DeclareOk result = channel.queueDeclare();
      return toJson(result);
    });
  }

  @Override
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<JsonObject>> resultHandler) {
    queueDeclare(queue, durable, exclusive, autoDelete, emptyConfig, resultHandler);
  }

  @Deprecated
  @Override
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, String> config, Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, channel -> {
      AMQP.Queue.DeclareOk result = channel.queueDeclare(queue, durable, exclusive, autoDelete, toArgumentsMap(config));
      return toJson(result);
    });
  }

  @Override
  public void queueDeclare(
    String queue,
    boolean durable,
    boolean exclusive,
    boolean autoDelete,
    JsonObject config,
    Handler<AsyncResult<JsonObject>> resultHandler
  ) {
    forChannel(resultHandler, channel -> {
      AMQP.Queue.DeclareOk result = channel.queueDeclare(queue, durable, exclusive, autoDelete, new LinkedHashMap<>(config.getMap()));
      return toJson(result);
    });
  }

  @Override
  public void queueDelete(String queue, Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, channel -> {
      AMQP.Queue.DeleteOk result = channel.queueDelete(queue);
      return toJson(result);
    });
  }

  @Override
  public void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, channel -> {
      AMQP.Queue.DeleteOk result = channel.queueDelete(queue, ifUnused, ifEmpty);
      return toJson(result);
    });
  }

  @Override
  public void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.queueBind(queue, exchange, routingKey);
      return null;
    });
  }

  @Override
  public void messageCount(String queue, Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, channel -> {
      Long result = channel.messageCount(queue);

      return new JsonObject().put("messageCount", result);
    });
  }

  @Override
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    log.info("Starting rabbitmq client");
    start(0, resultHandler);
  }

  private void start(int attempts, Handler<AsyncResult<Void>> resultHandler) {
    vertx.<Void>executeBlocking(future -> {
      try {
        connect();
        future.complete();
      } catch (IOException | TimeoutException e) {
        log.error("Could not connect to rabbitmq", e);
        future.fail(e);
      }
    }, ar -> {
      if (ar.succeeded() || retries == null) {
        resultHandler.handle(ar);
      } else if (attempts >= retries) {
        log.info("Max number of connect attempts (" + retries + ") reached. Will not attempt to connect again");
        resultHandler.handle(ar);
      } else {
        long delay = config.getConnectionRetryDelay();
        log.info("Attempting to reconnect to rabbitmq...");
        vertx.setTimer(delay, id -> {
          log.debug("Reconnect attempt # " + attempts);
          start(attempts + 1, resultHandler);
        });
      }
    });
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> resultHandler) {
    log.info("Stopping rabbitmq client");
    vertx.executeBlocking(future -> {
      try {
        disconnect();
        future.complete();
      } catch (IOException e) {
        future.fail(e);
      }
    }, resultHandler);
  }

  private <T> void forChannel(Handler<AsyncResult<T>> resultHandler, ChannelHandler<T> channelHandler) {
    if (connection == null || channel == null) {
      resultHandler.handle(Future.failedFuture("Not connected"));
      return;
    }
    if (!channel.isOpen()) {
      try {
        //TODO: Is this the best thing ?

        // change
        log.debug("channel is close, try create Channel");

        channel = connection.createChannel();

        if(channelConfirms)
          channel.confirmSelect();
      } catch (IOException e) {
        log.debug("create channel error");
        resultHandler.handle(Future.failedFuture(e));
      }
    }

    vertx.executeBlocking(future -> {
      try {
        T t = channelHandler.handle(channel);
        future.complete(t);
      } catch (Throwable t) {
        future.fail(t);
      }
    }, resultHandler);
  }

  private void connect() throws IOException, TimeoutException {
    log.debug("Connecting to rabbitmq...");
    connection = newConnection(config);
    connection.addShutdownListener(this);
    channel = connection.createChannel();
    log.debug("Connected to rabbitmq !");
  }

  private void disconnect() throws IOException {
    try {
      log.debug("Disconnecting from rabbitmq...");
      // This will close all channels related to this connection
      connection.close();
      log.debug("Disconnected from rabbitmq !");
    } finally {
      connection = null;
      channel = null;
    }
  }

  private Map<String, Object> toArgumentsMap(Map<String, String> map) {
    Map<String, Object> transformedMap = null;
    if (map != null) {
      transformedMap = new HashMap<>();
      map.forEach(transformedMap::put);
    }
    return transformedMap;
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException cause) {
    if (cause.isInitiatedByApplication()) {
      return;
    }

    log.info("RabbitMQ connection shutdown! The client will attempt to reconnect automatically", cause);
  }

  private interface ChannelHandler<T> {

    T handle(Channel channel) throws Exception;
  }
}
