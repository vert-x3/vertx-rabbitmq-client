package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rabbitmq.RabbitMQClient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.rabbitmq.impl.Utils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQClientImpl implements RabbitMQClient, ShutdownListener {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQClientImpl.class);

  private final Vertx vertx;
  private final JsonObject config;
  private final Integer retries;
  private final boolean includeProperties;

  private Connection connection;
  private Channel channel;

  public RabbitMQClientImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
    this.retries = config.getInteger("connectionRetries", null);
    //TODO: includeProperties isn't really intuitive
    //TODO: Think about allowing this at a method level ?
    this.includeProperties = config.getBoolean("includeProperties", false);
  }

  @Override
  public void basicAck(long deliveryTag, boolean multiple, Handler<AsyncResult<JsonObject>> resultHandler) {
    forChannel(resultHandler, (channel) -> {
      channel.basicAck(deliveryTag, multiple);
      return null;
    });
  }

  @Override
  public void basicConsume(String queue, String address, Handler<AsyncResult<Void>> resultHandler) {
    basicConsume(queue, address, true, resultHandler);
  }

  @Override
  public void basicConsume(String queue, String address, boolean autoAck, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.basicConsume(queue, new ConsumerHandler(vertx, channel, includeProperties, autoAck, ar -> {
        if (ar.succeeded()) {
          vertx.eventBus().send(address, ar.result());
        } else {
          log.error("Exception occurred inside rabbitmq service consumer.", ar.cause());
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
          put("properties", toJson(response.getProps()), json);
        }
        put("body", parse(response.getProps(), response.getBody()), json);
        put("messageCount", response.getMessageCount(), json);
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
  public void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler) {
    forChannel(resultHandler, channel -> {
      channel.exchangeDeclare(exchange, type, durable, autoDelete, null);
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
    forChannel(resultHandler, channel -> {
      AMQP.Queue.DeclareOk result = channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
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
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    log.info("Starting rabbitmq client");

    vertx.executeBlocking(future -> {
      try {
        connect();
        future.complete();
      } catch (IOException e) {
        log.error("Could not connect to rabbitmq", e);
        if (retries != null && retries > 0) {
          try {
            reconnect();
          } catch (IOException ioex) {
            future.fail(ioex);
          }
        } else {
          future.fail(e);
        }
      }
    }, resultHandler);
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
        channel = connection.createChannel();
      } catch (IOException e) {
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

  private void connect() throws IOException {
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

  private void reconnect() throws IOException {
    if (retries == null || retries < 1) return;

    log.info("Attempting to reconnect to rabbitmq...");
    AtomicInteger attempts = new AtomicInteger(0);
    int retries = this.retries;
    long delay = config.getLong("connectionRetryDelay", 10000L); // Every 10 seconds by default
    vertx.setPeriodic(delay, id -> {
      int attempt = attempts.incrementAndGet();
      if (attempt == retries) {
        vertx.cancelTimer(id);
        log.info("Max number of connect attempts (" + retries + ") reached. Will not attempt to connect again");
      } else {
        try {
          log.debug("Reconnect attempt # " + attempt);
          connect();
          vertx.cancelTimer(id);
          log.info("Successfully reconnected to rabbitmq (attempt # " + attempt + ")");
        } catch (IOException e) {
          log.debug("Failed to connect attempt # " + attempt, e);
        }
      }
    });
  }

  @Override
  public void shutdownCompleted(ShutdownSignalException cause) {
    if (cause.isInitiatedByApplication()) return;

    log.info("RabbitMQ connection shutdown !", cause);
    vertx.runOnContext(v -> {
      try {
        connection = null;
        channel = null;
        reconnect();
      } catch (IOException e) {
        log.error("IOException during reconnect.", e);
      }
    });
  }

  private static Connection newConnection(JsonObject config) throws IOException {
    ConnectionFactory cf = new ConnectionFactory();
    String uri = config.getString("uri");
    // Use uri if set, otherwise support individual connection parameters
    if (uri != null) {
      try {
        cf.setUri(uri);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid rabbitmq connection uri " + uri);
      }
    } else {
      String user = config.getString("user");
      if (user != null) {
        cf.setUsername(user);
      }
      String password = config.getString("password");
      if (password != null) {
        cf.setPassword(password);
      }
      String host = config.getString("host");
      if (host != null) {
        cf.setHost(host);
      }
      Integer port = config.getInteger("port");
      if (port != null) {
        cf.setPort(port);
      }
    }

    // Connection timeout
    Integer connectionTimeout = config.getInteger("connectionTimeout");
    if (connectionTimeout != null) {
      cf.setConnectionTimeout(connectionTimeout);
    }
    //TODO: Support other configurations

    return cf.newConnection();
  }

  private static interface ChannelHandler<T> {
    T handle(Channel channel) throws Exception;
  }
}
