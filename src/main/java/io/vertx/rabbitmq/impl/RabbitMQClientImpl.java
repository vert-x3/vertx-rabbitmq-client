package io.vertx.rabbitmq.impl;

import static io.vertx.rabbitmq.impl.Utils.toJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import io.netty.handler.ssl.JdkSslContext;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JdkSSLEngineOptions;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.streams.ReadStream;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConfirmation;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQClientImpl implements RabbitMQClient, ShutdownListener {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQClientImpl.class);
  private static final JsonObject emptyConfig = new JsonObject();
  
  private final Vertx vertx;
  private final RabbitMQOptions config;
  private final int retries;

  private Connection connection;
  private Channel channel;
  private long channelInstance;
  private boolean channelConfirms = false;
  private boolean hasConnected = false;
  
  private List<Handler<Promise<Void>>> connectionEstablishedCallbacks = new ArrayList<>();

  public RabbitMQClientImpl(Vertx vertx, RabbitMQOptions config) {
    this.vertx = vertx;
    this.config = config;
    this.retries = config.getReconnectAttempts();
  }

  public long getChannelInstance() {
    return channelInstance;
  }
  
  @Override
  public void addConnectionEstablishedCallback(Handler<Promise<Void>> connectionEstablishedCallback) {
    this.connectionEstablishedCallbacks.add(connectionEstablishedCallback);
  }
  
  private static Connection newConnection(Vertx vertx, RabbitMQOptions config) throws IOException, TimeoutException {
    ConnectionFactory cf = new ConnectionFactory();
    
    String uri = config.getUri();
    // Use uri if set, otherwise support individual connection parameters
    List<Address> addresses = null;
    if (uri != null) {
      try {
        log.info("Connecting to " + uri);
        cf.setUri(uri);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid rabbitmq connection uri ", e);
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

    if(config.isSsl()) {  
    	//The RabbitMQ Client connection needs a JDK SSLContext, so force this setting.
    	config.setSslEngineOptions(new JdkSSLEngineOptions());
    	SSLHelper sslHelper = new SSLHelper(config, config.getKeyCertOptions(), config.getTrustOptions());
    	JdkSslContext ctx = (JdkSslContext)sslHelper.getContext((VertxInternal)vertx);
      cf.useSslProtocol(ctx.context());
    }

    if (config.isNioEnabled()) {
      cf.useNio();
    }

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
    Future<Void> fut = basicAck(deliveryTag, multiple);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> basicAck(long deliveryTag, boolean multiple) {
    return forChannel((channel) -> {
      channel.basicAck(deliveryTag, multiple);
      return null;
    });
  }

  @Override
  public void basicNack(long deliveryTag, boolean multiple, boolean requeue, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = basicNack(deliveryTag, multiple, requeue);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> basicNack(long deliveryTag, boolean multiple, boolean requeue) {
    return forChannel((channel) -> {
      channel.basicNack(deliveryTag, multiple, requeue);
      return null;
    });
  }

  private void restartConsumer(int attempts, QueueConsumerHandler handler, QueueOptions options) {
    stop(ar -> {
      if (!handler.queue().isCancelled()) {
        if (ar.succeeded()) {    
          if (retries == 0) {
            log.error("Retries disabled. Will not attempt to reconnect");
          } else if (attempts >= retries) {
            log.error("Max number of consumer reconnect attempts (" + retries + ") reached. Will not attempt to reconnect again");
          } else {
            start((arStart) -> {
              if (arStart.succeeded()) {
                forChannel(chan -> {
                  RabbitMQConsumer q = handler.queue();
                  chan.basicConsume(q.queueName(), options.isAutoAck(), handler);
                  return q.resume();
                }).onComplete(arChan -> {
                  if (arChan.failed()) {
                    log.error("Failed to reconnect consumer: ", arChan.cause());
                    long delay = config.getReconnectInterval();
                    vertx.setTimer(delay, id -> {
                      restartConsumer(attempts + 1, handler, options);
                    });
                  }
                });
            } else {
              log.error("Failed to reconnect client: ", arStart.cause());
              long delay = config.getReconnectInterval();
              vertx.setTimer(delay, id -> {
                restartConsumer(attempts + 1, handler, options);
              });
            }
          });
          }
        } else {
          log.error("Failed to stop client, will not attempt to reconnect: ", ar.cause());
        }
      }
    });    
  }
  
  @Override
  public void basicConsumer(String queue, QueueOptions options, Handler<AsyncResult<RabbitMQConsumer>> resultHandler) {
    Future<RabbitMQConsumer> fut = basicConsumer(queue, options);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<RabbitMQConsumer> basicConsumer(String queue, QueueOptions options) {
    return forChannel(channel -> {
      log.debug("Created new QueueConsumer");
      QueueConsumerHandler handler = new QueueConsumerHandler(vertx, channel, options, queue);
      if (retries > 0) {
        handler.setShutdownHandler(sig -> {
          restartConsumer(0, handler, options);
        });
      }
      try {
        channel.basicConsume(queue, options.isAutoAck(), handler);
      } catch(Throwable ex) {
        log.warn("Failed to consume: ", ex);
        restartConsumer(0, handler, options);
      }
      return handler;
    }).map(res -> {
      RabbitMQConsumer q = res.queue();
      // Resume, pending elements will be delivered asynchronously providing the opportunity
      // for the handler to set a queue consumer or pause the queue
      q.resume();
      // Deliver to the application
      return q;
    });
  }  
  
  @Override
  public void basicGet(String queue, boolean autoAck, Handler<AsyncResult<RabbitMQMessage>> resultHandler) {
    Future<RabbitMQMessage> fut = basicGet(queue, autoAck);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<RabbitMQMessage> basicGet(String queue, boolean autoAck) {
    return forChannel(channel -> {
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
    basicPublishWithDeliveryTag(exchange, routingKey, new AMQP.BasicProperties(), body, null, resultHandler);
  }

  @Override
  public Future<Void> basicPublish(String exchange, String routingKey, Buffer body) {
    return basicPublishWithDeliveryTag(exchange, routingKey, new AMQP.BasicProperties(), body, null);
  }
  
  @Override
  public void basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body, Handler<AsyncResult<Void>> resultHandler) {
    basicPublishWithDeliveryTag(exchange, routingKey, properties, body, null, resultHandler);
  }
    
  @Override
  public Future<Void> basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body) {
    return basicPublishWithDeliveryTag(exchange, routingKey, properties, body, null);
  }

  @Override
  public void basicPublishWithDeliveryTag(String exchange, String routingKey, BasicProperties properties, Buffer body, Handler<Long> deliveryTagHandler, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = basicPublishWithDeliveryTag(exchange, routingKey, properties, body, deliveryTagHandler);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }
  
  @Override
  public Future<Void> basicPublishWithDeliveryTag(String exchange, String routingKey, BasicProperties properties, Buffer body, @Nullable Handler<Long> deliveryTagHandler) {
    return forChannel(channel -> {
      if (deliveryTagHandler != null) {
        long deliveryTag = channel.getNextPublishSeqNo();
        deliveryTagHandler.handle(deliveryTag);
      }
      channel.basicPublish(exchange, routingKey, (AMQP.BasicProperties) properties, body.getBytes());
      return null;
    });
  }
  
  @Override
  public void addConfirmListener(int maxQueueSize, Handler<AsyncResult<ReadStream<RabbitMQConfirmation>>> resultHandler) {
    Future<ReadStream<RabbitMQConfirmation>> fut = addConfirmListener(maxQueueSize);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<ReadStream<RabbitMQConfirmation>> addConfirmListener(int maxQueueSize) {
    return forChannel(channel -> {

      ChannelConfirmHandler handler = new ChannelConfirmHandler(vertx, this, maxQueueSize);
      channel.addConfirmListener(handler);
      channel.confirmSelect();

      channelConfirms = true;

      return handler.getListener();
    });
  }
  
  @Override
  public void confirmSelect(Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = confirmSelect();
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> confirmSelect() {
    return forChannel(channel -> {
      channel.confirmSelect();
      channelConfirms = true;
      return null;
    });
  }

  @Override
  public void waitForConfirms(Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = waitForConfirms();
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> waitForConfirms() {
    return forChannel(channel -> {
      channel.waitForConfirmsOrDie();
      return null;
    });
  }

  @Override
  public void waitForConfirms(long timeout, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = waitForConfirms(timeout);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> waitForConfirms(long timeout) {
    return forChannel(channel -> {
      channel.waitForConfirmsOrDie(timeout);
      return null;
    });
  }

  @Override
  public void basicQos(int prefetchSize, int prefetchCount, boolean global, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = basicQos(prefetchSize, prefetchCount, global);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> basicQos(int prefetchSize, int prefetchCount, boolean global) {
    return forChannel(channel -> {
      channel.basicQos(prefetchSize, prefetchCount, global);
      return null;
    });
  }

  @Override
  public void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = exchangeDeclare(exchange, type, durable, autoDelete);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete) {
    return exchangeDeclare(exchange, type, durable, autoDelete, emptyConfig);
  }

  @Override
  public void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, JsonObject config, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = exchangeDeclare(exchange, type, durable, autoDelete, config);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, JsonObject config) {
    return forChannel(channel -> {
      channel.exchangeDeclare(exchange, type, durable, autoDelete, new LinkedHashMap<>(config.getMap()));
      return null;
    });
  }

  @Override
  public void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = exchangeDelete(exchange);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> exchangeDelete(String exchange) {
    return forChannel(channel -> {
      channel.exchangeDelete(exchange);
      return null;
    });
  }

  @Override
  public void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = exchangeBind(destination, source, routingKey);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> exchangeBind(String destination, String source, String routingKey) {
    return forChannel(channel -> {
      channel.exchangeBind(destination, source, routingKey);
      return null;
    });
  }

  @Override
  public void exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = exchangeBind(destination, source, routingKey, arguments);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments) {
    return forChannel(channel -> {
      channel.exchangeBind(destination, source, routingKey, arguments);
      return null;
    });
  }

  @Override
  public void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = exchangeUnbind(destination, source, routingKey);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> exchangeUnbind(String destination, String source, String routingKey) {
    return forChannel(channel -> {
      channel.exchangeUnbind(destination, source, routingKey);
      return null;
    });
  }

  @Override
  public void exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = exchangeUnbind(destination, source, routingKey, arguments);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments) {
    return forChannel(channel -> {
      channel.exchangeUnbind(destination, source, routingKey, arguments);
      return null;
    });
  }

  @Override
  public void queueDeclareAuto(Handler<AsyncResult<JsonObject>> resultHandler) {
    Future<JsonObject> fut = queueDeclareAuto();
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<JsonObject> queueDeclareAuto() {
    return forChannel(channel -> {
      AMQP.Queue.DeclareOk result = channel.queueDeclare();
      return toJson(result);
    });
  }

  @Override
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<AMQP.Queue.DeclareOk>> resultHandler) {
    Future<AMQP.Queue.DeclareOk> fut = queueDeclare(queue, durable, exclusive, autoDelete);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<AMQP.Queue.DeclareOk> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete) {
    return queueDeclare(queue, durable, exclusive, autoDelete, emptyConfig);
  }

  @Override
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, JsonObject config, Handler<AsyncResult<AMQP.Queue.DeclareOk>> resultHandler) {
    Future<AMQP.Queue.DeclareOk> fut = queueDeclare(queue, durable, exclusive, autoDelete, config);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<AMQP.Queue.DeclareOk> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, JsonObject config) {
    return forChannel(channel -> channel.queueDeclare(queue, durable, exclusive, autoDelete, new LinkedHashMap<>(config.getMap())));
  }

  @Override
  public void queueDelete(String queue, Handler<AsyncResult<AMQP.Queue.DeleteOk>> resultHandler) {
    Future<AMQP.Queue.DeleteOk> fut = queueDelete(queue);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<AMQP.Queue.DeleteOk> queueDelete(String queue) {
    return forChannel(channel -> channel.queueDelete(queue));
  }

  @Override
  public void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<AMQP.Queue.DeleteOk>> resultHandler) {
    Future<AMQP.Queue.DeleteOk> fut = queueDeleteIf(queue, ifUnused, ifEmpty);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<AMQP.Queue.DeleteOk> queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty) {
    return forChannel(channel -> channel.queueDelete(queue, ifUnused, ifEmpty));
  }

  @Override
  public void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = queueBind(queue, exchange, routingKey);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> queueBind(String queue, String exchange, String routingKey) {
    return forChannel(channel -> {
      channel.queueBind(queue, exchange, routingKey);
      return null;
    });
  }

  @Override
  public void queueBind(String queue, String exchange, String routingKey, Map<String, Object> arguments, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = queueBind(queue, exchange, routingKey, arguments);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> queueBind(String queue, String exchange, String routingKey, Map<String, Object> arguments) {
    return forChannel(channel -> {
      channel.queueBind(queue, exchange, routingKey, arguments);
      return null;
    });
  }

  @Override
  public void queueUnbind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = queueUnbind(queue, exchange, routingKey);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> queueUnbind(String queue, String exchange, String routingKey) {
    return forChannel(channel -> {
      channel.queueUnbind(queue, exchange, routingKey);
      return null;
    });
  }

  @Override
  public void queueUnbind(String queue, String exchange, String routingKey, Map<String, Object> arguments, Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = queueUnbind(queue, exchange, routingKey, arguments);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> queueUnbind(String queue, String exchange, String routingKey, Map<String, Object> arguments) {
    return forChannel(channel -> {
      channel.queueUnbind(queue, exchange, routingKey, arguments);
      return null;
    });
  }

  @Override
  public void messageCount(String queue, Handler<AsyncResult<Long>> resultHandler) {
    Future<Long> fut = messageCount(queue);
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Long> messageCount(String queue) {
    return forChannel(channel -> channel.messageCount(queue));
  }

  @Override
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = start();
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> start() {
    log.info("Starting rabbitmq client");
    return start((ContextInternal) vertx.getOrCreateContext(), 0);
  }

  private Future<Void> start(ContextInternal ctx, int attempts) {
    return ctx.<Void>executeBlocking(promise -> {
      try {
        connect().onComplete(promise);
      } catch (IOException | TimeoutException e) {
        log.error("Could not connect to rabbitmq", e);
        promise.fail(e);
      }
    }).recover(err -> {
      if (retries == 0 || (!hasConnected && !config.isReconnectOnInitialConnection())) {
        log.error("Retries disabled. Will not attempt to reconnect");
        return ctx.failedFuture(err);
      } else if (attempts >= retries) {
        log.info("Max number of connect attempts (" + retries + ") reached. Will not attempt to connect again");
        return ctx.failedFuture(err);
      } else {
        long delay = config.getReconnectInterval();
        log.info("Attempting to reconnect to rabbitmq...");
        Promise<Void> promise = ctx.promise();
        vertx.setTimer(delay, id -> {
          log.debug("Reconnect attempt # " + attempts);
          start(ctx, attempts + 1).onComplete(promise);
        });
        return promise.future();
      }
    });
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> resultHandler) {
    Future<Void> fut = stop();
    if (resultHandler != null) {
      fut.onComplete(resultHandler);
    }
  }

  @Override
  public Future<Void> stop() {
    log.info("Stopping rabbitmq client");
    return vertx.executeBlocking(future -> {
      try {
        disconnect();
        future.complete();
      } catch (IOException e) {
        future.fail(e);
      }
    });
  }

  private <T> Future<T> forChannel(ChannelHandler<T> channelHandler) {
    ContextInternal ctx = (ContextInternal) vertx.getOrCreateContext();
    if (connection == null || channel == null) {
      return ctx.failedFuture("Not connected");
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
        return ctx.failedFuture(e);
      }
    }
    return vertx.executeBlocking(future -> {
      try {
        T t = channelHandler.handle(channel);
        future.complete(t);
      } catch (Throwable t) {
        future.fail(t);
      }
    });
  }

  private Future<Void> connect() throws IOException, TimeoutException {
    log.debug("Connecting to rabbitmq...");
    connection = newConnection(vertx, config);
    connection.addShutdownListener(this);
    ++channelInstance;
    channel = connection.createChannel();
    Promise promise = Promise.promise();
    if (connectionEstablishedCallbacks.isEmpty()) {
      promise.complete();
    } else {
      Iterator<Handler<Promise<Void>>> iter = connectionEstablishedCallbacks.iterator();
      connectCallbackHandler(Future.succeededFuture(), iter, promise);
    }
    log.debug("Connected to rabbitmq !");
    hasConnected = true;
    return promise.future();
  }

  private void connectCallbackHandler(AsyncResult<Void> prevResult, Iterator<Handler<Promise<Void>>> iter, Promise<Void> connectPromise) {
    try {
      if (prevResult.failed()) {
        connectPromise.fail(prevResult.cause());
      } else {
        if (iter.hasNext()) {        
          Handler<Promise<Void>> next = iter.next();
          Promise<Void> callbackPromise = Promise.promise();
          next.handle(callbackPromise);
          callbackPromise.future().onComplete(result -> connectCallbackHandler(result, iter, connectPromise));
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
