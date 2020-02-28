package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.*;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConfirmation;
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
  private long channelInstance;
  private boolean channelConfirms = false;
  
  private List<Handler<Promise<Void>>> connectionEstablishedCallbacks = new ArrayList<>();

  public RabbitMQClientImpl(Vertx vertx, RabbitMQOptions config) {
    this.vertx = vertx;
    this.config = config;
    this.retries = config.getConnectionRetries();
  }

  public long getChannelInstance() {
    return channelInstance;
  }
  
  @Override
  public void addConnectionEstablishedCallback(Handler<Promise<Void>> connectionEstablishedCallback) {
    this.connectionEstablishedCallbacks.add(connectionEstablishedCallback);
  }
  
  private static Connection newConnection(RabbitMQOptions config) throws IOException, TimeoutException {
    ConnectionFactory cf = new ConnectionFactory();
    String uri = config.getUri();
    // Use uri if set, otherwise support individual connection parameters
    List<Address> addresses = null;
    if (uri != null) {
      try {
        log.info("Connecting to " + uri);
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
  public Future<Void> basicAck(long deliveryTag, boolean multiple) {
    Promise<Void> promise = Promise.promise();
    basicAck(deliveryTag, multiple, promise);
    return promise.future();
  }

  @Override
  public void basicNack(long deliveryTag, boolean multiple, boolean requeue, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, (channel) -> {
      channel.basicNack(deliveryTag, multiple, requeue);
      return null;
    });
  }

  @Override
  public Future<Void> basicNack(long deliveryTag, boolean multiple, boolean requeue) {
    Promise<Void> promise = Promise.promise();
    basicNack(deliveryTag, multiple, requeue, promise);
    return promise.future();
  }

  private void restartConsumer(int attempts, QueueConsumerHandler handler, String queue, QueueOptions options) {
    stop(ar -> {
      if (!handler.queue().isCancelled()) {
        if (ar.succeeded()) {              
          if (attempts >= retries) {
            log.error("Max number of consumer restart attempts (" + retries + ") reached. Will not attempt to restart again");
          } else {
            start((arStart) -> {
            if (arStart.succeeded()) {
              forChannel(arChan -> {
                if (arChan.failed()) {
                  log.error("Failed to restart consumer: ", arChan.cause());
                  long delay = config.getConnectionRetryDelay();
                  vertx.setTimer(delay, id -> {
                    restartConsumer(attempts + 1, handler, queue, options);
                  });
                }
              }, chan -> {
                RabbitMQConsumer q = handler.queue();
                chan.basicConsume(queue, options.isAutoAck(), handler);
                return q.resume();
              });
            } else {
              log.error("Failed to restart client: ", arStart.cause());
              long delay = config.getConnectionRetryDelay();
              vertx.setTimer(delay, id -> {
                restartConsumer(attempts + 1, handler, queue, options);
              });
            }
          });
          }
        } else {
          log.error("Failed to stop client, will not attempt to restart: ", ar.cause());
        }
      }
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
      log.info("Created new QueueConsumer");
      QueueConsumerHandler handler = new QueueConsumerHandler(vertx, channel, options);
      handler.setShutdownHandler(sig -> {
        restartConsumer(0, handler, queue, options);
      });
      channel.basicConsume(queue, options.isAutoAck(), handler);
      return handler;
    });
  }
  
  @Override
  public Future<RabbitMQConsumer> basicConsumer(String queue, QueueOptions options) {
    Promise<RabbitMQConsumer> promise = Promise.promise();
    basicConsumer(queue, options, promise);
    return promise.future();
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
  public Future<RabbitMQMessage> basicGet(String queue, boolean autoAck) {
    Promise<RabbitMQMessage> promise = Promise.promise();
    basicGet(queue, autoAck, promise);
    return promise.future();
  }

  @Override
  public void basicPublish(String exchange, String routingKey, Buffer body, Handler<AsyncResult<Void>> resultHandler) {
    basicPublish(exchange, routingKey, new AMQP.BasicProperties(), body, resultHandler);
  }

  @Override
  public Future<Void> basicPublish(String exchange, String routingKey, Buffer body) {
    Promise<Void> promise = Promise.promise();
    basicPublishWithDeliveryTag(exchange, routingKey, new AMQP.BasicProperties(), body, null, promise);
    return promise.future();
  }
  
  @Override
  public void basicPublishWithDeliveryTag(String exchange, String routingKey, BasicProperties properties, Buffer body, Handler<Long> deliveryTagHandler, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      if (deliveryTagHandler != null) {
        long deliveryTag = channel.getNextPublishSeqNo();
        deliveryTagHandler.handle(deliveryTag);
      }
      channel.basicPublish(exchange, routingKey, (AMQP.BasicProperties) properties, body.getBytes());
      return null;
    });
  }
  
  @Override
  public Future<Void> basicPublishWithDeliveryTag(String exchange, String routingKey, BasicProperties properties, Buffer body, @Nullable Handler<Long> deliveryTagHandler) {
    Promise<Void> promise = Promise.promise();
    basicPublishWithDeliveryTag(exchange, routingKey, properties, body, deliveryTagHandler, promise);
    return promise.future();
  }
  
  @Override
  public void basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body, Handler<AsyncResult<Void>> resultHandler) {
    basicPublishWithDeliveryTag(exchange, routingKey, properties, body, null, resultHandler);
  }
    
  @Override
  public Future<Void> basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body) {
    Promise<Void> promise = Promise.promise();
    basicPublishWithDeliveryTag(exchange, routingKey, properties, body, null, promise);
    return promise.future();
  }

  @Override
  public void addConfirmListener(int maxQueueSize, Handler<AsyncResult<ReadStream<RabbitMQConfirmation>>> resultHandler) {
    forChannel(resultHandler, channel -> {

      ChannelConfirmHandler handler = new ChannelConfirmHandler(vertx, this, maxQueueSize);
      channel.addConfirmListener(handler);
      channel.confirmSelect();

      channelConfirms = true;

      return handler.getListener();
    });
  }

  @Override
  public Future<ReadStream<RabbitMQConfirmation>> addConfirmListener(int maxQueueSize) {
    Promise<ReadStream<RabbitMQConfirmation>> promise = Promise.promise();
    addConfirmListener(maxQueueSize, promise);
    return promise.future();
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
  public Future<Void> confirmSelect() {
    Promise<Void> promise = Promise.promise();
    confirmSelect(promise);
    return promise.future();
  }

  @Override
  public void waitForConfirms(Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.waitForConfirmsOrDie();

      return null;
    });
  }

  @Override
  public Future<Void> waitForConfirms() {
    Promise<Void> promise = Promise.promise();
    waitForConfirms(promise);
    return promise.future();
  }

  @Override
  public void waitForConfirms(long timeout, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.waitForConfirmsOrDie(timeout);

      return null;
    });
  }

  @Override
  public Future<Void> waitForConfirms(long timeout) {
    Promise<Void> promise = Promise.promise();
    waitForConfirms(timeout, promise);
    return promise.future();
  }

  @Override
  public void basicQos(int prefetchSize, int prefetchCount, boolean global, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.basicQos(prefetchSize, prefetchCount, global);
      return null;
    });
  }

  @Override
  public Future<Void> basicQos(int prefetchSize, int prefetchCount, boolean global) {
    Promise<Void> promise = Promise.promise();
    basicQos(prefetchSize, prefetchCount, global, promise);
    return promise.future();
  }

  @Override
  public void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler) {
    exchangeDeclare(exchange, type, durable, autoDelete, emptyConfig, resultHandler);
  }

  @Override
  public Future<Void> exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete) {
    Promise<Void> promise = Promise.promise();
    exchangeDeclare(exchange, type, durable, autoDelete, promise);
    return promise.future();
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
  public Future<Void> exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, JsonObject config) {
    Promise<Void> promise = Promise.promise();
    exchangeDeclare(exchange, type, durable, autoDelete, config, promise);
    return promise.future();
  }

  @Override
  public void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.exchangeDelete(exchange);
      return null;
    });
  }

  @Override
  public Future<Void> exchangeDelete(String exchange) {
    Promise<Void> promise = Promise.promise();
    exchangeDelete(exchange, promise);
    return promise.future();
  }

  @Override
  public void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.exchangeBind(destination, source, routingKey);
      return null;
    });
  }

  @Override
  public Future<Void> exchangeBind(String destination, String source, String routingKey) {
    Promise<Void> promise = Promise.promise();
    exchangeBind(destination, source, routingKey, promise);
    return promise.future();
  }

  @Override
  public void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.exchangeUnbind(destination, source, routingKey);
      return null;
    });
  }

  @Override
  public Future<Void> exchangeUnbind(String destination, String source, String routingKey) {
    Promise<Void> promise = Promise.promise();
    exchangeUnbind(destination, source, routingKey, promise);
    return promise.future();
  }

  @Override
  public void queueDeclareAuto(Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, channel -> {
      AMQP.Queue.DeclareOk result = channel.queueDeclare();
      return toJson(result);
    });
  }

  @Override
  public Future<JsonObject> queueDeclareAuto() {
    Promise<JsonObject> promise = Promise.promise();
    queueDeclareAuto(promise);
    return promise.future();
  }

  @Override
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<AMQP.Queue.DeclareOk>> resultHandler) {
    queueDeclare(queue, durable, exclusive, autoDelete, emptyConfig, resultHandler);
  }

  @Override
  public Future<AMQP.Queue.DeclareOk> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete) {
    Promise<AMQP.Queue.DeclareOk> promise = Promise.promise();
    queueDeclare(queue, durable, exclusive, autoDelete, promise);
    return promise.future();
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
  public Future<AMQP.Queue.DeclareOk> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, JsonObject config) {
    Promise<AMQP.Queue.DeclareOk> promise = Promise.promise();
    queueDeclare(queue, durable, exclusive, autoDelete, config, promise);
    return promise.future();
  }

  @Override
  public void queueDelete(String queue, Handler<AsyncResult<AMQP.Queue.DeleteOk>> resultHandler) {
    forChannel(resultHandler, channel -> channel.queueDelete(queue));
  }

  @Override
  public Future<AMQP.Queue.DeleteOk> queueDelete(String queue) {
    Promise<AMQP.Queue.DeleteOk> promise = Promise.promise();
    queueDelete(queue, promise);
    return promise.future();
  }

  @Override
  public void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<AMQP.Queue.DeleteOk>> resultHandler) {
    forChannel(resultHandler, channel -> channel.queueDelete(queue, ifUnused, ifEmpty));
  }

  @Override
  public Future<AMQP.Queue.DeleteOk> queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty) {
    Promise<AMQP.Queue.DeleteOk> promise = Promise.promise();
    queueDeleteIf(queue, ifUnused, ifEmpty, promise);
    return promise.future();
  }

  @Override
  public void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.queueBind(queue, exchange, routingKey);
      return null;
    });
  }

  @Override
  public Future<Void> queueBind(String queue, String exchange, String routingKey) {
    Promise<Void> promise = Promise.promise();
    queueBind(queue, exchange, routingKey, promise);
    return promise.future();
  }

  @Override
  public void messageCount(String queue, Handler<AsyncResult<Long>> resultHandler) {
    forChannel(resultHandler, channel -> channel.messageCount(queue));
  }

  @Override
  public Future<Long> messageCount(String queue) {
    Promise<Long> promise = Promise.promise();
    messageCount(queue, promise);
    return promise.future();
  }

  @Override
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    log.info("Starting rabbitmq client");
    start(0, resultHandler);
  }

  @Override
  public Future<Void> start() {
    Promise<Void> promise = Promise.promise();
    start(promise);
    return promise.future();
  }

  private void start(int attempts, Handler<AsyncResult<Void>> resultHandler) {
    vertx.<Void>executeBlocking(future -> {
      try {
        connect(future);
      } catch (IOException | TimeoutException e) {
        log.error("Could not connect to rabbitmq", e);
        future.fail(e);
      }
    }, ar -> {
      if (ar.succeeded() || retries == null) {
        resultHandler.handle(ar);
      } else {
        log.error("Failed to connect to rabbitmq: ", ar.cause());
        if (attempts >= retries) {
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

  @Override
  public Future<Void> stop() {
    Promise<Void> promise = Promise.promise();
    stop(promise);
    return promise.future();
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

        ++channelInstance;
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

  private Future connect(Promise<Void> promise) throws IOException, TimeoutException {
    log.debug("Connecting to rabbitmq...");
    connection = newConnection(config);
    connection.addShutdownListener(this);
    ++channelInstance;
    channel = connection.createChannel();
    Future result = promise.future();
    if (connectionEstablishedCallbacks.isEmpty()) {
      promise.complete();
    } else {
      Iterator<Handler<Promise<Void>>> iter = connectionEstablishedCallbacks.iterator();
      connectCallbackHandler(Future.succeededFuture(), iter, promise);
    }
    log.debug("Connected to rabbitmq !");
    return result;
  }

  private void connectCallbackHandler(AsyncResult<Void> prevResult, Iterator<Handler<Promise<Void>>> iter, Promise<Void> connectPromise) {
    try {
      if (prevResult.failed()) {
        connectPromise.fail(prevResult.cause());
      } else {
        if (iter.hasNext()) {        
          Handler<Promise<Void>> next = iter.next();
          Promise<Void> callbackPromise = Promise.promise();
          next.handle(connectPromise);
          callbackPromise.future().setHandler(result -> connectCallbackHandler(result, iter, connectPromise));
        } else {
          connectPromise.complete();
        }
      }
    } catch(Throwable ex) {
      log.error("Exception whilst running connection stablished callback: ", ex);
      connectPromise.fail(ex);
    }
  }

  private void disconnect() throws IOException {
    try {
      log.debug("Disconnecting from rabbitmq...");
      // This will close all channels related to this connection
      if (connection != null) {
        connection.close();
      }
      log.debug("Disconnected from rabbitmq !");
    } catch(AlreadyClosedException ex) {
      log.debug("Already disconnected from rabbitmq !");
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
