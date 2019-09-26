package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static io.vertx.rabbitmq.impl.Utils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQClientImpl implements RabbitMQClient, ShutdownListener {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQClientImpl.class);
  private static final JsonObject emptyConfig = new JsonObject();

  private final Vertx vertx;
  private final RabbitMQOptions config;
  private final Integer retries;

  private Connection connection;
  private Channel channel;
  private boolean channelConfirms = false;

  public RabbitMQClientImpl(Vertx vertx, RabbitMQOptions config) {
    this.vertx = vertx;
    this.config = config;
    this.retries = config.getConnectionRetries();
  }

  private static Connection newConnection(RabbitMQOptions config) throws IOException, TimeoutException {
    ConnectionFactory cf = new ConnectionFactory();
    String uri = config.getUri();
    // Use uri if set, otherwise support individual connection parameters
    List<Address> addresses = null;
    if (uri != null) {
      try {
        cf.setUri(uri);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid rabbitmq connection uri " + uri);
      }
    } else {
      cf.setUsername(config.getUser());
      cf.setPassword(config.getPassword());
      addresses = config.getAddresses().isEmpty()
                  ? Collections.singletonList(new Address(config.getHost(), config.getPort()))
                  : config.getAddresses();
      cf.setVirtualHost(config.getVirtualHost());
    }

    cf.setConnectionTimeout(config.getConnectionTimeout());
    cf.setRequestedHeartbeat(config.getRequestedHeartbeat());
    cf.setHandshakeTimeout(config.getHandshakeTimeout());
    cf.setRequestedChannelMax(config.getRequestedChannelMax());
    cf.setNetworkRecoveryInterval(config.getNetworkRecoveryInterval());
    cf.setAutomaticRecoveryEnabled(config.isAutomaticRecoveryEnabled());


    //TODO: Support other configurations

    return addresses == null
           ? cf.newConnection()
           : cf.newConnection(addresses);
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
  public void basicAck(long deliveryTag, boolean multiple, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, (channel) -> {
      channel.basicAck(deliveryTag, multiple);
      return null;
    });
  }

  @Override
  public void basicNack(long deliveryTag, boolean multiple, boolean requeue, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, (channel) -> {
      channel.basicNack(deliveryTag, multiple, requeue);
      return null;
    });
  }

  @Override
  public void basicConsumer(String queue, QueueOptions options, Handler<AsyncResult<RabbitMQConsumer>> resultHandler) {
    forChannel(ar -> {
      if (ar.succeeded()) {
        RabbitMQConsumer q = ar.result().queue();
        // Resume, pending elements will be delivered asynchronously providing the opportunity
        // for the handler to set a queue consumer or pause the queue
        q.resume();
        // Deliver to the application
        resultHandler.handle(Future.succeededFuture(q));
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    }, channel -> {
      QueueConsumerHandler handler = new QueueConsumerHandler(vertx, channel, options);
      String consumerTag = channel.basicConsume(queue, options.isAutoAck(), handler);
      return handler;
    });
  }

  @Override
  public void basicGet(String queue, boolean autoAck, Handler<AsyncResult<RabbitMQMessage>> resultHandler) {
    forChannel(resultHandler, (channel) -> {
      GetResponse response = channel.basicGet(queue, autoAck);
      if (response == null) {
        return null;
      } else {
        return new RabbitMQMessageImpl(response.getBody(), null, response.getEnvelope(), response.getProps(), response.getMessageCount());
      }
    });
  }

  @Override
  public void basicPublish(String exchange, String routingKey, Buffer body, Handler<AsyncResult<Void>> resultHandler) {
    basicPublish(exchange, routingKey, new AMQP.BasicProperties(), body, resultHandler);
  }

  @Override
  public void basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.basicPublish(exchange, routingKey, (AMQP.BasicProperties) properties, body.getBytes());
      return null;
    });  }

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
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<AMQP.Queue.DeclareOk>> resultHandler) {
    queueDeclare(queue, durable, exclusive, autoDelete, emptyConfig, resultHandler);
  }

  @Override
  public void queueDeclare(
    String queue,
    boolean durable,
    boolean exclusive,
    boolean autoDelete,
    JsonObject config,
    Handler<AsyncResult<AMQP.Queue.DeclareOk>> resultHandler
  ) {
    forChannel(resultHandler, channel -> channel.queueDeclare(queue, durable, exclusive, autoDelete, new LinkedHashMap<>(config.getMap())));
  }

  @Override
  public void queueDelete(String queue, Handler<AsyncResult<AMQP.Queue.DeleteOk>> resultHandler) {
    forChannel(resultHandler, channel -> channel.queueDelete(queue));
  }

  @Override
  public void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<AMQP.Queue.DeleteOk>> resultHandler) {
    forChannel(resultHandler, channel -> channel.queueDelete(queue, ifUnused, ifEmpty));
  }

  @Override
  public void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.queueBind(queue, exchange, routingKey);
      return null;
    });
  }

  @Override
  public void messageCount(String queue, Handler<AsyncResult<Long>> resultHandler) {
    forChannel(resultHandler, channel -> channel.messageCount(queue));
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
