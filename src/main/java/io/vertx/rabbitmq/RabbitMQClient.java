package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.impl.RabbitMQClientImpl;

import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@VertxGen
public interface RabbitMQClient {

  /**
   * Create and return a client.
   *
   * @param vertx the vertx instance
   * @param config the client config
   * @return the client
   */
  static RabbitMQClient create(Vertx vertx, RabbitMQOptions config) {
    return new RabbitMQClientImpl(vertx, config);
  }

  /**
   * Like {@link #create(Vertx, RabbitMQOptions)} but with a {@link JsonObject} config object.
   */

  @GenIgnore
  static RabbitMQClient create(Vertx vertx, JsonObject config) {
    return new RabbitMQClientImpl(vertx, new RabbitMQOptions(config));
  }

  //TODO: Think about splitting this out into different API's with specific roles (admin, pub, sub)
  //TODO: Simplify/Change name of API methods to match more vert.x type verbiage ?

  /**
   * Acknowledge one or several received messages. Supply the deliveryTag from the AMQP.Basic.GetOk or AMQP.Basic.Deliver
   * method containing the received message being acknowledged.
   *
   * @see com.rabbitmq.client.Channel#basicAck(long, boolean)
   */
  void basicAck(long deliveryTag, boolean multiple, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Reject one or several received messages.
   *
   * @see com.rabbitmq.client.Channel#basicNack(long, boolean, boolean)
   */
  void basicNack(long deliveryTag, boolean multiple, boolean requeue, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Retrieve a message from a queue using AMQP.Basic.Get
   *
   * @see com.rabbitmq.client.Channel#basicGet(String, boolean)
   */
  void basicGet(String queue, boolean autoAck, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Use {@link RabbitMQClient#basicConsumer(String, QueueOptions, Handler)} instead
   * <p>
   * Start a non-nolocal, non-exclusive consumer, with auto acknowledgement and a server-generated consumerTag.
   *
   * @see com.rabbitmq.client.Channel#basicConsume(String, Consumer)
   */
  @Deprecated
  void basicConsume(String queue, String address, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Use {@link RabbitMQClient#basicConsumer(String, QueueOptions, Handler)} instead
   * <p>
   * Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag.
   *
   * @see com.rabbitmq.client.Channel#basicConsume(String, boolean, String, Consumer)
   */
  @Deprecated
  void basicConsume(String queue, String address, boolean autoAck, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Use {@link RabbitMQClient#basicConsumer(String, QueueOptions, Handler)} instead
   * <p>
   * Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag and error handler
   *
   * @see com.rabbitmq.client.Channel#basicConsume(String, boolean, String, Consumer)
   */
  @Deprecated
  void basicConsume(String queue, String address, boolean autoAck, Handler<AsyncResult<Void>> resultHandler, Handler<Throwable> errorHandler);

  /**
   * @see com.rabbitmq.client.Channel#basicConsume(String, Consumer)
   * @see RabbitMQClient#basicConsumer(String, Handler)
   */
  default void basicConsumer(String queue, Handler<AsyncResult<RabbitMQConsumer>> resultHandler) {
    basicConsumer(queue, new QueueOptions(), resultHandler);
  }

  /**
   * Create a consumer with the given {@code options}.
   *
   * @param queue          the name of a queue
   * @param options        options for queue
   * @param resultHandler  a handler through which you can find out the operation status;
   *                       if the operation succeeds you can begin to receive messages
   *                       through an instance of {@link RabbitMQConsumer}
   * @see com.rabbitmq.client.Channel#basicConsume(String, boolean, String, Consumer)
   */
  void basicConsumer(String queue, QueueOptions options, Handler<AsyncResult<RabbitMQConsumer>> resultHandler);

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   *
   * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
   */
  void basicPublish(String exchange, String routingKey, JsonObject message, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Enables publisher acknowledgements on this channel. Can be called once during client initialisation. Calls to basicPublish()
   * will have to be confirmed.
   *
   * @see Channel#confirmSelect()
   * @see http://www.rabbitmq.com/confirms.html
   */
  void confirmSelect(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Wait until all messages published since the last call have been either ack'd or nack'd by the broker.
   * This will incur slight performance loss at the expense of higher write consistency.
   * If desired, multiple calls to basicPublish() can be batched before confirming.
   *
   * @see Channel#waitForConfirms()
   * @see http://www.rabbitmq.com/confirms.html
   *
   * @throws java.io.IOException Throws an IOException if the message was not written to the queue.
   */
  void waitForConfirms(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Wait until all messages published since the last call have been either ack'd or nack'd by the broker; or until timeout elapses. If the timeout expires a TimeoutException is thrown.
   *
   * @param timeout
   *
   * @see io.vertx.rabbitmq.impl.RabbitMQClientImpl#waitForConfirms(Handler)
   * @see http://www.rabbitmq.com/confirms.html
   *
   * @throws java.io.IOException Throws an IOException if the message was not written to the queue.
   */
  void waitForConfirms(long timeout, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Request a specific prefetchCount "quality of service" settings
   * for this channel.
   *
   * @see #basicQos(int, int, boolean, Handler)
   * @param prefetchCount maximum number of messages that the server
   * will deliver, 0 if unlimited
   * @param resultHandler handler called when operation is done with a result of the operation
   */
  default void basicQos(int prefetchCount, Handler<AsyncResult<Void>> resultHandler) {
    basicQos(prefetchCount, false, resultHandler);
  }

  /**
   * Request a specific prefetchCount "quality of service" settings
   * for this channel.
   *
   * @see #basicQos(int, int, boolean, Handler)
   * @param prefetchCount maximum number of messages that the server
   * will deliver, 0 if unlimited
   * @param global true if the settings should be applied to the
   * entire channel rather than each consumer
   * @param resultHandler handler called when operation is done with a result of the operation
   */
  default void basicQos(int prefetchCount, boolean global, Handler<AsyncResult<Void>> resultHandler) {
    basicQos(0, prefetchCount, global, resultHandler);
  }

  /**
   * Request specific "quality of service" settings.
   *
   * These settings impose limits on the amount of data the server
   * will deliver to consumers before requiring acknowledgements.
   * Thus they provide a means of consumer-initiated flow control.
   * @see com.rabbitmq.client.AMQP.Basic.Qos
   * @param prefetchSize maximum amount of content (measured in
   * octets) that the server will deliver, 0 if unlimited
   * @param prefetchCount maximum number of messages that the server
   * will deliver, 0 if unlimited
   * @param global true if the settings should be applied to the
   * entire channel rather than each consumer
   * @param resultHandler handler called when operation is done with a result of the operation
   */
  void basicQos(int prefetchSize, int prefetchCount, boolean global, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Declare an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean, boolean, Map)
   */
  void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Declare an exchange with additional parameters such as dead lettering or an alternate exchnage.
   *
   * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean, boolean, Map)
   * @see #exchangeDeclare(String, String, boolean, boolean, JsonObject, Handler)
   * @deprecated Use {@link #exchangeDeclare(String, String, boolean, boolean, JsonObject, Handler)} instead for
   * support for more than just String config values
   */
  @GenIgnore
  @Deprecated
  void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Map<String, String> config, Handler<AsyncResult<Void>> resultHandler);


  /**
   * Declare an exchange with additional parameters such as dead lettering, an alternate exchange or TTL.
   *
   * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean, boolean, Map)
   */
  void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, JsonObject config, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Delete an exchange, without regard for whether it is in use or not.
   *
   * @see com.rabbitmq.client.Channel#exchangeDelete(String)
   */
  void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Bind an exchange to an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeBind(String, String, String)
   */
  void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Unbind an exchange from an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeUnbind(String, String, String)
   */
  void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Actively declare a server-named exclusive, autodelete, non-durable queue.
   *
   * @see com.rabbitmq.client.Channel#queueDeclare()
   */
  //TODO: Auto ?
  void queueDeclareAuto(Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Declare a queue
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   */
  void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Declare a queue with config options
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   * @see #queueDeclare(String, boolean, boolean, boolean, JsonObject, Handler)
   * @deprecated See {@link #queueDeclare(String, boolean, boolean, boolean, JsonObject, Handler)} instead for
   * support for more than just String config values
   */
  @GenIgnore
  @Deprecated
  void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, String> config, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Declare a queue with config options
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   */
  void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, JsonObject config, Handler<AsyncResult<JsonObject>> resultHandler);


  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String)
   */
  void queueDelete(String queue, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Delete a queue
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String, boolean, boolean)
   */
  void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Bind a queue to an exchange
   *
   * @see com.rabbitmq.client.Channel#queueBind(String, String, String)
   */
  void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Returns the number of messages in a queue ready to be delivered.
   *
   * @see com.rabbitmq.client.Channel#messageCount(String)
   */
  void messageCount(String queue, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Start the rabbitMQ client. Create the connection and the chanel.
   *
   * @see com.rabbitmq.client.Connection#createChannel()
   */
  void start(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Stop the rabbitMQ client. Close the connection and its chanel.
   *
   * @see com.rabbitmq.client.Connection#close()
   */
  void stop(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Check if a connection is open
   *
   * @return true when the connection is open, false otherwise
   * @see com.rabbitmq.client.ShutdownNotifier#isOpen()
   */
  boolean isConnected();

  /**
   * Check if a channel is open
   *
   * @return true when the connection is open, false otherwise
   * @see com.rabbitmq.client.ShutdownNotifier#isOpen()
   */
  boolean isOpenChannel();
}
