/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.rxjava.rabbitmq;

import java.util.Map;
import rx.Observable;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.rabbitmq.RabbitMQClient original} non RX-ified interface using Vert.x codegen.
 */

public class RabbitMQClient {

  final io.vertx.rabbitmq.RabbitMQClient delegate;

  public RabbitMQClient(io.vertx.rabbitmq.RabbitMQClient delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public static RabbitMQClient create(Vertx vertx, JsonObject config) { 
    RabbitMQClient ret = RabbitMQClient.newInstance(io.vertx.rabbitmq.RabbitMQClient.create((io.vertx.core.Vertx)vertx.getDelegate(), config));
    return ret;
  }

  /**
   * Acknowledge one or several received messages. Supply the deliveryTag from the AMQP.Basic.GetOk or AMQP.Basic.Deliver
   * method containing the received message being acknowledged.
   * @param deliveryTag 
   * @param multiple 
   * @param resultHandler 
   */
  public void basicAck(long deliveryTag, boolean multiple, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.basicAck(deliveryTag, multiple, resultHandler);
  }

  /**
   * Acknowledge one or several received messages. Supply the deliveryTag from the AMQP.Basic.GetOk or AMQP.Basic.Deliver
   * method containing the received message being acknowledged.
   * @param deliveryTag 
   * @param multiple 
   * @return 
   */
  public Observable<JsonObject> basicAckObservable(long deliveryTag, boolean multiple) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    basicAck(deliveryTag, multiple, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Reject one or several received messages.
   * @param deliveryTag 
   * @param multiple 
   * @param requeue 
   * @param resultHandler 
   */
  public void basicNack(long deliveryTag, boolean multiple, boolean requeue, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.basicNack(deliveryTag, multiple, requeue, resultHandler);
  }

  /**
   * Reject one or several received messages.
   * @param deliveryTag 
   * @param multiple 
   * @param requeue 
   * @return 
   */
  public Observable<JsonObject> basicNackObservable(long deliveryTag, boolean multiple, boolean requeue) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    basicNack(deliveryTag, multiple, requeue, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Retrieve a message from a queue using AMQP.Basic.Get
   * @param queue 
   * @param autoAck 
   * @param resultHandler 
   */
  public void basicGet(String queue, boolean autoAck, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.basicGet(queue, autoAck, resultHandler);
  }

  /**
   * Retrieve a message from a queue using AMQP.Basic.Get
   * @param queue 
   * @param autoAck 
   * @return 
   */
  public Observable<JsonObject> basicGetObservable(String queue, boolean autoAck) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    basicGet(queue, autoAck, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Start a non-nolocal, non-exclusive consumer, with auto acknowledgement and a server-generated consumerTag.
   * @param queue 
   * @param address 
   * @param resultHandler 
   */
  public void basicConsume(String queue, String address, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.basicConsume(queue, address, resultHandler);
  }

  /**
   * Start a non-nolocal, non-exclusive consumer, with auto acknowledgement and a server-generated consumerTag.
   * @param queue 
   * @param address 
   * @return 
   */
  public Observable<Void> basicConsumeObservable(String queue, String address) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    basicConsume(queue, address, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag.
   * @param queue 
   * @param address 
   * @param autoAck 
   * @param resultHandler 
   */
  public void basicConsume(String queue, String address, boolean autoAck, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.basicConsume(queue, address, autoAck, resultHandler);
  }

  /**
   * Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag.
   * @param queue 
   * @param address 
   * @param autoAck 
   * @return 
   */
  public Observable<Void> basicConsumeObservable(String queue, String address, boolean autoAck) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    basicConsume(queue, address, autoAck, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   * @param exchange 
   * @param routingKey 
   * @param message 
   * @param resultHandler 
   */
  public void basicPublish(String exchange, String routingKey, JsonObject message, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.basicPublish(exchange, routingKey, message, resultHandler);
  }

  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   * @param exchange 
   * @param routingKey 
   * @param message 
   * @return 
   */
  public Observable<Void> basicPublishObservable(String exchange, String routingKey, JsonObject message) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    basicPublish(exchange, routingKey, message, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Request specific "quality of service" settings, Limiting the number of unacknowledged messages on
   * a channel (or connection). This limit is applied separately to each new consumer on the channel.
   * @param prefetchCount 
   * @param resultHandler 
   */
  public void basicQos(int prefetchCount, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.basicQos(prefetchCount, resultHandler);
  }

  /**
   * Request specific "quality of service" settings, Limiting the number of unacknowledged messages on
   * a channel (or connection). This limit is applied separately to each new consumer on the channel.
   * @param prefetchCount 
   * @return 
   */
  public Observable<Void> basicQosObservable(int prefetchCount) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    basicQos(prefetchCount, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Declare an exchange.
   * @param exchange 
   * @param type 
   * @param durable 
   * @param autoDelete 
   * @param resultHandler 
   */
  public void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.exchangeDeclare(exchange, type, durable, autoDelete, resultHandler);
  }

  /**
   * Declare an exchange.
   * @param exchange 
   * @param type 
   * @param durable 
   * @param autoDelete 
   * @return 
   */
  public Observable<Void> exchangeDeclareObservable(String exchange, String type, boolean durable, boolean autoDelete) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    exchangeDeclare(exchange, type, durable, autoDelete, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Delete an exchange, without regard for whether it is in use or not.
   * @param exchange 
   * @param resultHandler 
   */
  public void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.exchangeDelete(exchange, resultHandler);
  }

  /**
   * Delete an exchange, without regard for whether it is in use or not.
   * @param exchange 
   * @return 
   */
  public Observable<Void> exchangeDeleteObservable(String exchange) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    exchangeDelete(exchange, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   *  Bind an exchange to an exchange.
   * @param destination 
   * @param source 
   * @param routingKey 
   * @param resultHandler 
   */
  public void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.exchangeBind(destination, source, routingKey, resultHandler);
  }

  /**
   *  Bind an exchange to an exchange.
   * @param destination 
   * @param source 
   * @param routingKey 
   * @return 
   */
  public Observable<Void> exchangeBindObservable(String destination, String source, String routingKey) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    exchangeBind(destination, source, routingKey, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Unbind an exchange from an exchange.
   * @param destination 
   * @param source 
   * @param routingKey 
   * @param resultHandler 
   */
  public void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.exchangeUnbind(destination, source, routingKey, resultHandler);
  }

  /**
   * Unbind an exchange from an exchange.
   * @param destination 
   * @param source 
   * @param routingKey 
   * @return 
   */
  public Observable<Void> exchangeUnbindObservable(String destination, String source, String routingKey) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    exchangeUnbind(destination, source, routingKey, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Actively declare a server-named exclusive, autodelete, non-durable queue.
   * @param resultHandler 
   */
  public void queueDeclareAuto(Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.queueDeclareAuto(resultHandler);
  }

  /**
   * Actively declare a server-named exclusive, autodelete, non-durable queue.
   * @return 
   */
  public Observable<JsonObject> queueDeclareAutoObservable() { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    queueDeclareAuto(resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Declare a queue
   * @param queue 
   * @param durable 
   * @param exclusive 
   * @param autoDelete 
   * @param resultHandler 
   */
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.queueDeclare(queue, durable, exclusive, autoDelete, resultHandler);
  }

  /**
   * Declare a queue
   * @param queue 
   * @param durable 
   * @param exclusive 
   * @param autoDelete 
   * @return 
   */
  public Observable<JsonObject> queueDeclareObservable(String queue, boolean durable, boolean exclusive, boolean autoDelete) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    queueDeclare(queue, durable, exclusive, autoDelete, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   * @param queue 
   * @param resultHandler 
   */
  public void queueDelete(String queue, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.queueDelete(queue, resultHandler);
  }

  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   * @param queue 
   * @return 
   */
  public Observable<JsonObject> queueDeleteObservable(String queue) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    queueDelete(queue, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Delete a queue
   * @param queue 
   * @param ifUnused 
   * @param ifEmpty 
   * @param resultHandler 
   */
  public void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.queueDeleteIf(queue, ifUnused, ifEmpty, resultHandler);
  }

  /**
   * Delete a queue
   * @param queue 
   * @param ifUnused 
   * @param ifEmpty 
   * @return 
   */
  public Observable<JsonObject> queueDeleteIfObservable(String queue, boolean ifUnused, boolean ifEmpty) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    queueDeleteIf(queue, ifUnused, ifEmpty, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Bind a queue to an exchange
   * @param queue 
   * @param exchange 
   * @param routingKey 
   * @param resultHandler 
   */
  public void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.queueBind(queue, exchange, routingKey, resultHandler);
  }

  /**
   * Bind a queue to an exchange
   * @param queue 
   * @param exchange 
   * @param routingKey 
   * @return 
   */
  public Observable<Void> queueBindObservable(String queue, String exchange, String routingKey) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    queueBind(queue, exchange, routingKey, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Returns the number of messages in a queue ready to be delivered.
   * @param queue 
   * @param resultHandler 
   */
  public void messageCount(String queue, Handler<AsyncResult<JsonObject>> resultHandler) { 
    delegate.messageCount(queue, resultHandler);
  }

  /**
   * Returns the number of messages in a queue ready to be delivered.
   * @param queue 
   * @return 
   */
  public Observable<JsonObject> messageCountObservable(String queue) { 
    io.vertx.rx.java.ObservableFuture<JsonObject> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    messageCount(queue, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Start the rabbitMQ client. Create the connection and the chanel.
   * @param resultHandler 
   */
  public void start(Handler<AsyncResult<Void>> resultHandler) { 
    delegate.start(resultHandler);
  }

  /**
   * Start the rabbitMQ client. Create the connection and the chanel.
   * @return 
   */
  public Observable<Void> startObservable() { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    start(resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Stop the rabbitMQ client. Close the connection and its chanel.
   * @param resultHandler 
   */
  public void stop(Handler<AsyncResult<Void>> resultHandler) { 
    delegate.stop(resultHandler);
  }

  /**
   * Stop the rabbitMQ client. Close the connection and its chanel.
   * @return 
   */
  public Observable<Void> stopObservable() { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    stop(resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Check if a connection is open
   * @return true when the connection is open, false otherwise
   */
  public boolean isConnected() { 
    boolean ret = delegate.isConnected();
    return ret;
  }

  /**
   * Check if a channel is open
   * @return true when the connection is open, false otherwise
   */
  public boolean isOpenChannel() { 
    boolean ret = delegate.isOpenChannel();
    return ret;
  }


  public static RabbitMQClient newInstance(io.vertx.rabbitmq.RabbitMQClient arg) {
    return arg != null ? new RabbitMQClient(arg) : null;
  }
}
