package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Consumer;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.impl.RabbitMQClientImpl;

import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@VertxGen
public interface RabbitMQClient {
  
  /**
   * Create and return a client configured with the default options.
   *
   * @param vertx the vertx instance
   * @return the client
   */
  static RabbitMQClient create(Vertx vertx) {
    return new RabbitMQClientImpl(vertx, new RabbitMQOptions());
  }

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
   * Set a callback to be called whenever a new connection is established.
   * This callback must be idempotent - it will be called each time a connection is established, which may be multiple times against the same instance.
   * Callbacks will be added to a list and called in the order they were added, the only way to remove callbacks is to create a new client.
   * @param connectionEstablishedCallback  callback to be called whenever a new connection is established.
   */
  @GenIgnore
  void addConnectionEstablishedCallback(Handler<RabbitMQClient> connectionEstablishedCallback);
  
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
  void basicAck(long deliveryTag, boolean multiple, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #basicAck(long, boolean, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> basicAck(long deliveryTag, boolean multiple);

  /**
   * Reject one or several received messages.
   *
   * @see com.rabbitmq.client.Channel#basicNack(long, boolean, boolean)
   */
  void basicNack(long deliveryTag, boolean multiple, boolean requeue, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #basicNack(long, boolean, boolean, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> basicNack(long deliveryTag, boolean multiple, boolean requeue);

  /**
   * Retrieve a message from a queue using AMQP.Basic.Get
   *
   * @see com.rabbitmq.client.Channel#basicGet(String, boolean)
   */
  void basicGet(String queue, boolean autoAck, Handler<AsyncResult<RabbitMQMessage>> resultHandler);

  /**
   * Like {@link #basicGet(String, boolean, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<RabbitMQMessage> basicGet(String queue, boolean autoAck);

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
   * Like {@link #basicConsumer(String, QueueOptions, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<RabbitMQConsumer> basicConsumer(String queue, QueueOptions options);

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   *
   * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
   */
  void basicPublish(String exchange, String routingKey, Buffer body, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #basicPublish(String, String, Buffer, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> basicPublish(String exchange, String routingKey, Buffer body);

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   *
   * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  void basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #basicPublish(String, String, BasicProperties, Buffer, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<Void> basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body);

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   *
   * @param deliveryTagHandler callback to capture the deliveryTag for this message.  Note that this will be called synchronously in the context of the client.
   * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  void basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body, @Nullable Handler<Long> deliveryTagHandler, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Add a Confirm Listener to the channel.
   * Note that this will automatically call confirmSelect, it is not necessary to call that too.
   *
   * @param maxQueueSize   maximum size of the queue of confirmations
   * @param resultHandler  a handler through which you can find out the operation status;
   *                       if the operation succeeds you can begin to receive confirmations 
   *                       through an instance of {@link RabbitMQConfirmListener}
   * @see com.rabbitmq.client.Channel#addConfirmListener(ConfirmListener)
   */
  void addConfirmListener(int maxQueueSize, Handler<AsyncResult<RabbitMQConfirmListener>> resultHandler);

  /**
   * Add a Confirm Listener to the channel.
   * Like {@link #addConfirmListener(Handler)} but returns a {@code Future} of the asynchronous result
   * Note that this will automatically call confirmSelect, it is not necessary to call that too.
   *
   * @param maxQueueSize   maximum size of the queue of confirmations
   * @return a future through which you can find out the operation status;
   *                       if the operation succeeds you can begin to receive confirmations 
   *                       through an instance of {@link RabbitMQConfirmListener}
   * @see com.rabbitmq.client.Channel#addConfirmListener(ConfirmListener)
   */
  Future<RabbitMQConfirmListener> addConfirmListener(int maxQueueSize);

  /**
   * Enables publisher acknowledgements on this channel. Can be called once during client initialisation. Calls to basicPublish()
   * will have to be confirmed.
   *
   * @see Channel#confirmSelect()
   * @see http://www.rabbitmq.com/confirms.html
   */
  void confirmSelect(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #confirmSelect(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> confirmSelect();

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
   * Like {@link #waitForConfirms(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> waitForConfirms();

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
   * Like {@link #waitForConfirms(long, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> waitForConfirms(long timeout);

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
   * Like {@link #basicQos(int, Handler)} but returns a {@code Future} of the asynchronous result
   */
  default Future<Void> basicQos(int prefetchCount) {
    return basicQos(prefetchCount, false);
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
   * Like {@link #basicQos(int, boolean, Handler)} but returns a {@code Future} of the asynchronous result
   */
  default Future<Void> basicQos(int prefetchCount, boolean global) {
    return basicQos(0, prefetchCount, global);
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
   * Like {@link #basicQos(int, int, boolean, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> basicQos(int prefetchSize, int prefetchCount, boolean global);

  /**
   * Declare an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean, boolean, Map)
   */
  void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #exchangeDeclare(String, String, boolean, boolean, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete);

  /**
   * Declare an exchange with additional parameters such as dead lettering, an alternate exchange or TTL.
   *
   * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean, boolean, Map)
   */
  void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, JsonObject config, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #exchangeDeclare(String, String, boolean, boolean, JsonObject, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, JsonObject config);

  /**
   * Delete an exchange, without regard for whether it is in use or not.
   *
   * @see com.rabbitmq.client.Channel#exchangeDelete(String)
   */
  void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #exchangeDelete(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> exchangeDelete(String exchange);

  /**
   * Bind an exchange to an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeBind(String, String, String)
   */
  void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #exchangeBind(String, String, String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> exchangeBind(String destination, String source, String routingKey);

  /**
   * Unbind an exchange from an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeUnbind(String, String, String)
   */
  void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #exchangeUnbind(String, String, String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> exchangeUnbind(String destination, String source, String routingKey);

  /**
   * Actively declare a server-named exclusive, autodelete, non-durable queue.
   *
   * @see com.rabbitmq.client.Channel#queueDeclare()
   */
  //TODO: Auto ?
  void queueDeclareAuto(Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Like {@link #queueDeclareAuto(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<JsonObject> queueDeclareAuto();

  /**
   * Declare a queue
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<AMQP.Queue.DeclareOk>> resultHandler);

  /**
   * Like {@link #queueDeclare(String, boolean, boolean, boolean, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<AMQP.Queue.DeclareOk> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete);

  /**
   * Declare a queue with config options
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, JsonObject config, Handler<AsyncResult<AMQP.Queue.DeclareOk>> resultHandler);

  /**
   * Like {@link #queueDeclare(String, boolean, boolean, boolean, JsonObject, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<AMQP.Queue.DeclareOk> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, JsonObject config);

  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String)
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  void queueDelete(String queue, Handler<AsyncResult<AMQP.Queue.DeleteOk>> resultHandler);

  /**
   * Like {@link #queueDelete(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<AMQP.Queue.DeleteOk> queueDelete(String queue);

  /**
   * Delete a queue
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String, boolean, boolean)
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<AMQP.Queue.DeleteOk>> resultHandler);

  /**
   * Like {@link #queueDeleteIf(String, boolean, boolean, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<AMQP.Queue.DeleteOk> queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty);

  /**
   * Bind a queue to an exchange
   *
   * @see com.rabbitmq.client.Channel#queueBind(String, String, String)
   */
  void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #queueBind(String, String, String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> queueBind(String queue, String exchange, String routingKey);

  /**
   * Returns the number of messages in a queue ready to be delivered.
   *
   * @see com.rabbitmq.client.Channel#messageCount(String)
   */
  void messageCount(String queue, Handler<AsyncResult<Long>> resultHandler);

  /**
   * Like {@link #messageCount(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Long> messageCount(String queue);

  /**
   * Start the rabbitMQ client. Create the connection and the chanel.
   *
   * @see com.rabbitmq.client.Connection#createChannel()
   */
  void start(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #start(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> start();

  /**
   * Stop the rabbitMQ client. Close the connection and its chanel.
   *
   * @see com.rabbitmq.client.Connection#close()
   */
  void stop(Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #stop(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Void> stop();

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
