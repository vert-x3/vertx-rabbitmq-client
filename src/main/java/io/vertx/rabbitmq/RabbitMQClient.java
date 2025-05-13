package io.vertx.rabbitmq;

import com.rabbitmq.client.*;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
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
   *
   * These callbacks should be used to establish any Rabbit MQ server objects that are required - exchanges, queues, bindings, etc.
   * Each callback will receive a Promise<Void> that it must complete in order to pass control to the next callback (or back to the RabbitMQClient).
   * If the callback fails the promise the RabbitMQClient will be unable to make a connection (it will attempt to connect again according to its retry configuration).
   * If the promise is not completed or failed by a callback the RabbitMQClient will not start (it will hang indefinitely).
   *
   * Other methods on the client may be used in the callback -
   * it is specifically expected that RabbitMQ objects will be declared, but the publish and consume methods must not be used.
   *
   * The connection established callbacks are particularly important with the RabbitMQPublisher and RabbitMQConsumer when they are used with
   * servers that may failover to another instance of the server that does not have the same exchanges/queues configured on it.
   * In this situation these callbacks are the only opportunity to create exchanges, queues and bindings before the client will attempt to use them when it
   * re-establishes connection.
   * If your failover cluster is guaranteed to have the appropriate objects already configured then it is not necessary to use the callbacks.
   *
   * @param connectionEstablishedCallback  callback to be called whenever a new connection is established.
   */
  @GenIgnore
  void addConnectionEstablishedCallback(Handler<Promise<Void>> connectionEstablishedCallback);

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
  Future<Void> basicAck(long deliveryTag, boolean multiple);

  /**
   * Reject one or several received messages.
   *
   * @see com.rabbitmq.client.Channel#basicNack(long, boolean, boolean)
   */
  Future<Void> basicNack(long deliveryTag, boolean multiple, boolean requeue);

  /**
   * Retrieve a message from a queue using AMQP.Basic.Get
   *
   * @see com.rabbitmq.client.Channel#basicGet(String, boolean)
   */
  Future<RabbitMQMessage> basicGet(String queue, boolean autoAck);

 /**
   * @see com.rabbitmq.client.Channel#basicConsume(String, Consumer)
  * @see RabbitMQClient#basicConsumer(String)
   */
  default Future<RabbitMQConsumer> basicConsumer(String queue) {
    return basicConsumer(queue, new QueueOptions());
  }

  /**
   * Create a consumer with the given {@code options}.
   *
   * @param queue          the name of a queue
   * @param options        options for queue
   * @return a future completed with the operation status; if the operation succeeds you can begin to receive messages through an instance of {@link RabbitMQConsumer}
   * @see com.rabbitmq.client.Channel#basicConsume(String, boolean, String, Consumer)
   */
  Future<RabbitMQConsumer> basicConsumer(String queue, QueueOptions options);

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   *
   * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
   */
  Future<Void> basicPublish(String exchange, String routingKey, Buffer body);

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   *
   * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<Void> basicPublish(String exchange, String routingKey, BasicProperties properties, Buffer body);

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   *
   * The deliveryTagHandler will be called before the message is sent, which is necessary because the confirmation may arrive
   * asynchronously before the resultHandler is called.
   *
   * @param deliveryTagHandler callback to capture the deliveryTag for this message.
   *        Note that this will be called synchronously in the context of the client before the result is known.
   * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<Void> basicPublishWithDeliveryTag(String exchange, String routingKey, BasicProperties properties, Buffer body, @Nullable Handler<Long> deliveryTagHandler);

  /**
   * Add a Confirm Listener to the channel.
   * Note that this will automatically call confirmSelect, it is not necessary to call that too.
   *
   * @param maxQueueSize   maximum size of the queue of confirmations
   * @return a future completed with a stream of confirmations  if the operation succeeds
   * @see com.rabbitmq.client.Channel#addConfirmListener(ConfirmListener)
   */
  Future<ReadStream<RabbitMQConfirmation>> addConfirmListener(int maxQueueSize);

  /**
   * Enables publisher acknowledgements on this channel. Can be called once during client initialisation. Calls to basicPublish()
   * will have to be confirmed.
   *
   * @see Channel#confirmSelect()
   * @link <a href="http://www.rabbitmq.com/confirms.html">Confirms</a>
   */
  Future<Void> confirmSelect();

  /**
   * Wait until all messages published since the last call have been either ack'd or nack'd by the broker.
   * This will incur slight performance loss at the expense of higher write consistency.
   * If desired, multiple calls to basicPublish() can be batched before confirming.
   *
   * @see Channel#waitForConfirms()
   * @link <a href="http://www.rabbitmq.com/confirms.html">Confirms</a>
   *
   * @throws java.io.IOException Throws an IOException if the message was not written to the queue.
   */
  Future<Void> waitForConfirms();

  /**
   * Wait until all messages published since the last call have been either ack'd or nack'd by the broker; or until timeout elapses. If the timeout expires a TimeoutException is thrown.
   *
   * @param timeout
   *
   * @see RabbitMQClient#waitForConfirms()
   * @link <a href="http://www.rabbitmq.com/confirms.html">Confirms</a>
   *
   * @throws java.io.IOException Throws an IOException if the message was not written to the queue.
   */
  Future<Void> waitForConfirms(long timeout);

  /**
   * Request a specific prefetchCount "quality of service" settings
   * for this channel.
   *
   * @see #basicQos(int, int, boolean)
   * @param prefetchCount maximum number of messages that the server
   * will deliver, 0 if unlimited
   */
  default Future<Void> basicQos(int prefetchCount) {
    return basicQos(prefetchCount, false);
  }

  /**
   * Request a specific prefetchCount "quality of service" settings
   * for this channel.
   *
   * @see #basicQos(int, int, boolean)
   * @param prefetchCount maximum number of messages that the server
   * will deliver, 0 if unlimited
   * @param global true if the settings should be applied to the
   * entire channel rather than each consumer
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
   */
  Future<Void> basicQos(int prefetchSize, int prefetchCount, boolean global);

  /**
   * Declare an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean, boolean, Map)
   */
  Future<Void> exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete);

  /**
   * Declare an exchange with additional parameters such as dead lettering, an alternate exchange or TTL.
   *
   * @see com.rabbitmq.client.Channel#exchangeDeclare(String, String, boolean, boolean, Map)
   */
  Future<Void> exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, JsonObject config);

  /**
   * Delete an exchange, without regard for whether it is in use or not.
   *
   * @see com.rabbitmq.client.Channel#exchangeDelete(String)
   */
  Future<Void> exchangeDelete(String exchange);

  /**
   * Bind an exchange to an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeBind(String, String, String)
   */
  Future<Void> exchangeBind(String destination, String source, String routingKey);

  /**
   * Bind an exchange to an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeBind(String, String, String, Map<String, Object>)
   */
  Future<Void> exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments);

  /**
   * Unbind an exchange from an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeUnbind(String, String, String)
   */
  Future<Void> exchangeUnbind(String destination, String source, String routingKey);

  /**
   * Unbind an exchange from an exchange.
   *
   * @see com.rabbitmq.client.Channel#exchangeUnbind(String, String, String, Map<String, Object>)
   */
  Future<Void> exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments);

  /**
   * Actively declare a server-named exclusive, autodelete, non-durable queue.
   *
   * @see com.rabbitmq.client.Channel#queueDeclare()
   */
  Future<JsonObject> queueDeclareAuto();

  /**
   * Declare a queue
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<AMQP.Queue.DeclareOk> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete);

  /**
   * Declare a queue with config options
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<AMQP.Queue.DeclareOk> queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, JsonObject config);

  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String)
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<AMQP.Queue.DeleteOk> queueDelete(String queue);

  /**
   * Delete a queue
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String, boolean, boolean)
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<AMQP.Queue.DeleteOk> queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty);

  /**
   * Bind a queue to an exchange
   *
   * @see com.rabbitmq.client.Channel#queueBind(String, String, String)
   */
  Future<Void> queueBind(String queue, String exchange, String routingKey);

  /**
   * Bind a queue to an exchange
   *
   * @see com.rabbitmq.client.Channel#queueBind(String, String, String, Map<String, Object>)
   */
  Future<Void> queueBind(String queue, String exchange, String routingKey, Map<String, Object> arguments);

  /**
   * Unbind a queue from an exchange
   *
   * @see com.rabbitmq.client.Channel#queueUnbind(String, String, String)
   */
  Future<Void> queueUnbind(String queue, String exchange, String routingKey);

  /**
   * Unbind a queue from an exchange
   *
   * @see com.rabbitmq.client.Channel#queueUnbind(String, String, String, Map<String, Object>)
   */
  Future<Void> queueUnbind(String queue, String exchange, String routingKey, Map<String, Object> arguments);

  /**
   * Returns the number of messages in a queue ready to be delivered.
   *
   * @see com.rabbitmq.client.Channel#messageCount(String)
   */
  Future<Long> messageCount(String queue);

  /**
   * Start the rabbitMQ client. Create the connection and the channel.
   *
   * @see com.rabbitmq.client.Connection#createChannel()
   */
  Future<Void> start();

  /**
   * Stop the rabbitMQ client. Close the connection and its channel.
   *
   * @see com.rabbitmq.client.Connection#close()
   */
  Future<Void> stop();

  /**
   * Check if a connection is open
   *
   * @return true when the connection is open, false otherwise
   * @see com.rabbitmq.client.ShutdownNotifier#isOpen()
   */
  boolean isConnected();

  /***
   * restart the rabbitMQ connect.
   * @param attempts  number of attempts
   * @return a future notified when the operation is done with a result of the operation
   */
  Future<Void> restartConnect(int attempts);

  /**
   * Check if a channel is open
   *
   * @return true when the connection is open, false otherwise
   * @see com.rabbitmq.client.ShutdownNotifier#isOpen()
   */
  boolean isOpenChannel();
}
