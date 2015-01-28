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
import io.vertx.groovy.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@CompileStatic
public class RabbitMQService {
  final def io.vertx.rabbitmq.RabbitMQService delegate;
  public RabbitMQService(io.vertx.rabbitmq.RabbitMQService delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static RabbitMQService create(Vertx vertx, Map<String, Object> config) {
    def ret= RabbitMQService.FACTORY.apply(io.vertx.rabbitmq.RabbitMQService.create((io.vertx.core.Vertx)vertx.getDelegate(), config != null ? new io.vertx.core.json.JsonObject(config) : null));
    return ret;
  }
  public static RabbitMQService createEventBusProxy(Vertx vertx, String address) {
    def ret= RabbitMQService.FACTORY.apply(io.vertx.rabbitmq.RabbitMQService.createEventBusProxy((io.vertx.core.Vertx)vertx.getDelegate(), address));
    return ret;
  }
  public void basicGet(String queue, boolean autoAck, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.basicGet(queue, autoAck, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void basicConsume(String queue, String address, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.basicConsume(queue, address, resultHandler);
  }
  public void basicPublish(String exchange, String routingKey, Map<String, Object> message, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.basicPublish(exchange, routingKey, message != null ? new io.vertx.core.json.JsonObject(message) : null, resultHandler);
  }
  public void exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.exchangeDeclare(exchange, type, durable, autoDelete, resultHandler);
  }
  public void exchangeDelete(String exchange, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.exchangeDelete(exchange, resultHandler);
  }
  public void exchangeBind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.exchangeBind(destination, source, routingKey, resultHandler);
  }
  public void exchangeUnbind(String destination, String source, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.exchangeUnbind(destination, source, routingKey, resultHandler);
  }
  /**
   * Actively declare a server-named exclusive, autodelete, non-durable queue.
   *
   * @see com.rabbitmq.client.Channel#queueDeclare()
   */
  public void queueDeclareAuto(Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.queueDeclareAuto(new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Declare a queue
   *
   * @see com.rabbitmq.client.Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)
   */
  public void queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.queueDeclare(queue, durable, exclusive, autoDelete, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Delete a queue, without regard for whether it is in use or has messages on it
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String)
   */
  public void queueDelete(String queue, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.queueDelete(queue, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Delete a queue
   *
   * @see com.rabbitmq.client.Channel#queueDelete(String, boolean, boolean)
   */
  public void queueDeleteIf(String queue, boolean ifUnused, boolean ifEmpty, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.queueDeleteIf(queue, ifUnused, ifEmpty, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result(event.result()?.getMap())
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  /**
   * Bind a queue to an exchange
   *
   * @see com.rabbitmq.client.Channel#queueBind(String, String, String)
   */
  public void queueBind(String queue, String exchange, String routingKey, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.queueBind(queue, exchange, routingKey, resultHandler);
  }
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.start(resultHandler);
  }
  public void stop(Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.stop(resultHandler);
  }

  static final java.util.function.Function<io.vertx.rabbitmq.RabbitMQService, RabbitMQService> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.rabbitmq.RabbitMQService arg -> new RabbitMQService(arg);
  };
}
