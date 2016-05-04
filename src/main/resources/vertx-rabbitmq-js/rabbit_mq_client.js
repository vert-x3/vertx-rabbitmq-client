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

/** @module vertx-rabbitmq-js/rabbit_mq_client */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRabbitMQClient = io.vertx.rabbitmq.RabbitMQClient;

/**

 @class
*/
var RabbitMQClient = function(j_val) {

  var j_rabbitMQClient = j_val;
  var that = this;

  /**
   Acknowledge one or several received messages. Supply the deliveryTag from the AMQP.Basic.GetOk or AMQP.Basic.Deliver
   method containing the received message being acknowledged.

   @public
   @param deliveryTag {number} 
   @param multiple {boolean} 
   @param resultHandler {function} 
   */
  this.basicAck = function(deliveryTag, multiple, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] ==='number' && typeof __args[1] ==='boolean' && typeof __args[2] === 'function') {
      j_rabbitMQClient["basicAck(long,boolean,io.vertx.core.Handler)"](deliveryTag, multiple, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Reject one or several received messages.

   @public
   @param deliveryTag {number} 
   @param multiple {boolean} 
   @param requeue {boolean} 
   @param resultHandler {function} 
   */
  this.basicNack = function(deliveryTag, multiple, requeue, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] ==='number' && typeof __args[1] ==='boolean' && typeof __args[2] ==='boolean' && typeof __args[3] === 'function') {
      j_rabbitMQClient["basicNack(long,boolean,boolean,io.vertx.core.Handler)"](deliveryTag, multiple, requeue, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Retrieve a message from a queue using AMQP.Basic.Get

   @public
   @param queue {string} 
   @param autoAck {boolean} 
   @param resultHandler {function} 
   */
  this.basicGet = function(queue, autoAck, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] ==='boolean' && typeof __args[2] === 'function') {
      j_rabbitMQClient["basicGet(java.lang.String,boolean,io.vertx.core.Handler)"](queue, autoAck, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag.

   @public
   @param queue {string} 
   @param address {string} 
   @param autoAck {boolean} 
   @param resultHandler {function} 
   */
  this.basicConsume = function() {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'function') {
      j_rabbitMQClient["basicConsume(java.lang.String,java.lang.String,io.vertx.core.Handler)"](__args[0], __args[1], function(ar) {
      if (ar.succeeded()) {
        __args[2](null, null);
      } else {
        __args[2](null, ar.cause());
      }
    });
    }  else if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] ==='boolean' && typeof __args[3] === 'function') {
      j_rabbitMQClient["basicConsume(java.lang.String,java.lang.String,boolean,io.vertx.core.Handler)"](__args[0], __args[1], __args[2], function(ar) {
      if (ar.succeeded()) {
        __args[3](null, null);
      } else {
        __args[3](null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
   which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.

   @public
   @param exchange {string} 
   @param routingKey {string} 
   @param message {Object} 
   @param resultHandler {function} 
   */
  this.basicPublish = function(exchange, routingKey, message, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_rabbitMQClient["basicPublish(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](exchange, routingKey, utils.convParamJsonObject(message), function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Request specific "quality of service" settings, Limiting the number of unacknowledged messages on
   a channel (or connection). This limit is applied separately to each new consumer on the channel.

   @public
   @param prefetchCount {number} 
   @param resultHandler {function} 
   */
  this.basicQos = function(prefetchCount, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='number' && typeof __args[1] === 'function') {
      j_rabbitMQClient["basicQos(int,io.vertx.core.Handler)"](prefetchCount, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Declare an exchange.

   @public
   @param exchange {string} 
   @param type {string} 
   @param durable {boolean} 
   @param autoDelete {boolean} 
   @param resultHandler {function} 
   */
  this.exchangeDeclare = function(exchange, type, durable, autoDelete, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] ==='boolean' && typeof __args[3] ==='boolean' && typeof __args[4] === 'function') {
      j_rabbitMQClient["exchangeDeclare(java.lang.String,java.lang.String,boolean,boolean,io.vertx.core.Handler)"](exchange, type, durable, autoDelete, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Delete an exchange, without regard for whether it is in use or not.

   @public
   @param exchange {string} 
   @param resultHandler {function} 
   */
  this.exchangeDelete = function(exchange, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_rabbitMQClient["exchangeDelete(java.lang.String,io.vertx.core.Handler)"](exchange, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
    Bind an exchange to an exchange.

   @public
   @param destination {string} 
   @param source {string} 
   @param routingKey {string} 
   @param resultHandler {function} 
   */
  this.exchangeBind = function(destination, source, routingKey, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'function') {
      j_rabbitMQClient["exchangeBind(java.lang.String,java.lang.String,java.lang.String,io.vertx.core.Handler)"](destination, source, routingKey, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Unbind an exchange from an exchange.

   @public
   @param destination {string} 
   @param source {string} 
   @param routingKey {string} 
   @param resultHandler {function} 
   */
  this.exchangeUnbind = function(destination, source, routingKey, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'function') {
      j_rabbitMQClient["exchangeUnbind(java.lang.String,java.lang.String,java.lang.String,io.vertx.core.Handler)"](destination, source, routingKey, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Actively declare a server-named exclusive, autodelete, non-durable queue.

   @public
   @param resultHandler {function} 
   */
  this.queueDeclareAuto = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_rabbitMQClient["queueDeclareAuto(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Declare a queue

   @public
   @param queue {string} 
   @param durable {boolean} 
   @param exclusive {boolean} 
   @param autoDelete {boolean} 
   @param resultHandler {function} 
   */
  this.queueDeclare = function(queue, durable, exclusive, autoDelete, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] ==='boolean' && typeof __args[2] ==='boolean' && typeof __args[3] ==='boolean' && typeof __args[4] === 'function') {
      j_rabbitMQClient["queueDeclare(java.lang.String,boolean,boolean,boolean,io.vertx.core.Handler)"](queue, durable, exclusive, autoDelete, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Delete a queue, without regard for whether it is in use or has messages on it

   @public
   @param queue {string} 
   @param resultHandler {function} 
   */
  this.queueDelete = function(queue, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_rabbitMQClient["queueDelete(java.lang.String,io.vertx.core.Handler)"](queue, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Delete a queue

   @public
   @param queue {string} 
   @param ifUnused {boolean} 
   @param ifEmpty {boolean} 
   @param resultHandler {function} 
   */
  this.queueDeleteIf = function(queue, ifUnused, ifEmpty, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] ==='boolean' && typeof __args[2] ==='boolean' && typeof __args[3] === 'function') {
      j_rabbitMQClient["queueDeleteIf(java.lang.String,boolean,boolean,io.vertx.core.Handler)"](queue, ifUnused, ifEmpty, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Bind a queue to an exchange

   @public
   @param queue {string} 
   @param exchange {string} 
   @param routingKey {string} 
   @param resultHandler {function} 
   */
  this.queueBind = function(queue, exchange, routingKey, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'function') {
      j_rabbitMQClient["queueBind(java.lang.String,java.lang.String,java.lang.String,io.vertx.core.Handler)"](queue, exchange, routingKey, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Start the rabbitMQ client. Create the connection and the chanel.

   @public
   @param resultHandler {function} 
   */
  this.start = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_rabbitMQClient["start(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Stop the rabbitMQ client. Close the connection and its chanel.

   @public
   @param resultHandler {function} 
   */
  this.stop = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_rabbitMQClient["stop(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Check if a connection is open

   @public

   @return {boolean} true when the connection is open, false otherwise
   */
  this.isConnected = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_rabbitMQClient["isConnected()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Check if a channel is open

   @public

   @return {boolean} true when the connection is open, false otherwise
   */
  this.isOpenChannel = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return j_rabbitMQClient["isOpenChannel()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_rabbitMQClient;
};

/**

 @memberof module:vertx-rabbitmq-js/rabbit_mq_client
 @param vertx {Vertx} 
 @param config {Object} 
 @return {RabbitMQClient}
 */
RabbitMQClient.create = function(vertx, config) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(JRabbitMQClient["create(io.vertx.core.Vertx,io.vertx.core.json.JsonObject)"](vertx._jdel, utils.convParamJsonObject(config)), RabbitMQClient);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = RabbitMQClient;