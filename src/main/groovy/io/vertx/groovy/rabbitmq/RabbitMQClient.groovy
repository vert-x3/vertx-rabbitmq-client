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

package io.vertx.groovy.rabbitmq;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
*/
@CompileStatic
public class RabbitMQClient {
  private final def io.vertx.rabbitmq.RabbitMQClient delegate;
  public RabbitMQClient(Object delegate) {
    this.delegate = (io.vertx.rabbitmq.RabbitMQClient) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static RabbitMQClient create(Vertx vertx, Map<String, Object> config) {
    def ret= InternalHelper.safeCreate(io.vertx.rabbitmq.RabbitMQClient.create((io.vertx.core.Vertx)vertx.getDelegate(), config != null ? new io.vertx.core.json.JsonObject(config) : null), io.vertx.groovy.rabbitmq.RabbitMQClient.class);
    return ret;
  }
  /**
   * Acknowledge one or several received messages. Supply the deliveryTag from the AMQP.Basic.GetOk or AMQP.Basic.Deliver
   * method containing the received message being acknowledged.
   * @param deliveryTag 
   * @param multiple 
   * @param resultHandler 
   */
  public void basicAck(long deliveryTag, boolean multiple, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.basicAck(deliveryTag, multiple, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Reject one or several received messages.
   * @param deliveryTag 
   * @param multiple 
   * @param requeue 
   * @param resultHandler 
   */
  public void basicNack(long deliveryTag, boolean multiple, boolean requeue, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.basicNack(deliveryTag, multiple, requeue, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Retrieve a message from a queue using AMQP.Basic.Get
   * @param queue 
   * @param autoAck 
   * @param resultHandler 
   */
  public void basicGet(String queue, boolean autoAck, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.basicGet(queue, autoAck, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Start a non-nolocal, non-exclusive consumer, with auto acknowledgement and a server-generated consumerTag.
   * @param queue 
   * @param address 
   * @param resultHandler 
   */
  public void basicConsume(String queue, String address, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.basicConsume(queue, address, resultHandler);
  }
  /**
   * Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag.
   * @param queue 
   * @param address 
   * @param autoAck 
   * @param resultHandler 
   */
  public void basicConsume(String queue, String address, boolean autoAck, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.basicConsume(queue, address, autoAck, resultHandler);
  }
  /**
   * Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   * which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
   * @param exchange 
   * @param routingKey 
   * @param message 
   * @param resultHandler 
   */
  public void basicPublish(String exchange, String routingKey, Map<String, Object> message, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.basicPublish(exchange, routingKey, message != null ? new io.vertx.core.json.JsonObject(message) : null, resultHandler);
  }
  /**
   * Request specific "quality of service" settings, Limiting the number of unacknowledged messages on
   * a channel (or connection). This limit is applied separately to each new consumer on the channel.
   * @param prefetchCount 
   * @param resultHandler 
   */
  public void basicQos(int prefetchCount, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.basicQos(prefetchCount, resultHandler);
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
    this.delegate.exchangeDeclare(exchange, type, durable, autoDelete, resultHandler);
  }
  /**
   * Delete an exchange, without regard for whether it is in use or not.
   * @param exchange 
   * @param resultHandler 
   */
  public void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.exchangeDelete(exchange, resultHandler);
  }
  /**
   *  Bind an exchange to an exchange.
   * @param destination 
   * @param source 
   * @param routingKey 
   * @param resultHandler 
   */
  public void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.exchangeBind(destination, source, routingKey, resultHandler);
  }
  /**
   * Unbind an exchange from an exchange.
   * @param destination 
   * @param source 
   * @param routingKey 
   * @param resultHandler 
   */
  public void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.exchangeUnbind(destination, source, routingKey, resultHandler);
  }
  /**
   * Actively declare a server-named exclusive, autodelete, non-durable queue.
   * @param resultHandler 
   */
  public void queueDeclareAuto(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.queueDeclareAuto(new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Declare a queue
   * @param queue 
   * @param durable 
   * @param exclusive 
   * @param autoDelete 
   * @param resultHandler 
   */
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.queueDeclare(queue, durable, exclusive, autoDelete, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   * @param queue 
   * @param resultHandler 
   */
  public void queueDelete(String queue, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.queueDelete(queue, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Delete a queue
   * @param queue 
   * @param ifUnused 
   * @param ifEmpty 
   * @param resultHandler 
   */
  public void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.queueDeleteIf(queue, ifUnused, ifEmpty, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Bind a queue to an exchange
   * @param queue 
   * @param exchange 
   * @param routingKey 
   * @param resultHandler 
   */
  public void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.queueBind(queue, exchange, routingKey, resultHandler);
  }
  /**
   * Returns the number of messages in a queue ready to be delivered.
   * @param queue 
   * @param resultHandler 
   */
  public void messageCount(String queue, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.messageCount(queue, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Start the rabbitMQ client. Create the connection and the chanel.
   * @param resultHandler 
   */
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.start(resultHandler);
  }
  /**
   * Stop the rabbitMQ client. Close the connection and its chanel.
   * @param resultHandler 
   */
  public void stop(Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.stop(resultHandler);
  }
  /**
   * Check if a connection is open
   * @return true when the connection is open, false otherwise
   */
  public boolean isConnected() {
    def ret = this.delegate.isConnected();
    return ret;
  }
  /**
   * Check if a channel is open
   * @return true when the connection is open, false otherwise
   */
  public boolean isOpenChannel() {
    def ret = this.delegate.isOpenChannel();
    return ret;
  }
}
