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

/** @module vertx-rabbitmq-js/rabbit_mq_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JRabbitMQService = io.vertx.rabbitmq.RabbitMQService;

/**

 @class
*/
var RabbitMQService = function(j_val) {

  var j_rabbitMQService = j_val;
  var that = this;

  /**

   @public
   @param queue {string} 
   @param autoAck {boolean} 
   @param resultHandler {function} 
   */
  this.basicGet = function(queue, autoAck, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] ==='boolean' && typeof __args[2] === 'function') {
      j_rabbitMQService["basicGet(java.lang.String,boolean,io.vertx.core.Handler)"](queue, autoAck, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param queue {string} 
   @param address {string} 
   @param resultHandler {function} 
   */
  this.basicConsume = function(queue, address, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'function') {
      j_rabbitMQService["basicConsume(java.lang.String,java.lang.String,io.vertx.core.Handler)"](queue, address, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param exchange {string} 
   @param routingKey {string} 
   @param message {Object} 
   @param resultHandler {function} 
   */
  this.basicPublish = function(exchange, routingKey, message, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'object' && typeof __args[3] === 'function') {
      j_rabbitMQService["basicPublish(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](exchange, routingKey, utils.convParamJsonObject(message), function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

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
      j_rabbitMQService["exchangeDeclare(java.lang.String,java.lang.String,boolean,boolean,io.vertx.core.Handler)"](exchange, type, durable, autoDelete, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param exchange {string} 
   @param resultHandler {function} 
   */
  this.exchangeDelete = function(exchange, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_rabbitMQService["exchangeDelete(java.lang.String,io.vertx.core.Handler)"](exchange, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param destination {string} 
   @param source {string} 
   @param routingKey {string} 
   @param resultHandler {function} 
   */
  this.exchangeBind = function(destination, source, routingKey, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'function') {
      j_rabbitMQService["exchangeBind(java.lang.String,java.lang.String,java.lang.String,io.vertx.core.Handler)"](destination, source, routingKey, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param destination {string} 
   @param source {string} 
   @param routingKey {string} 
   @param resultHandler {function} 
   */
  this.exchangeUnbind = function(destination, source, routingKey, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && typeof __args[3] === 'function') {
      j_rabbitMQService["exchangeUnbind(java.lang.String,java.lang.String,java.lang.String,io.vertx.core.Handler)"](destination, source, routingKey, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Actively declare a server-named exclusive, autodelete, non-durable queue.

   @public
   @param resultHandler {function} 
   */
  this.queueDeclareAuto = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_rabbitMQService["queueDeclareAuto(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
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
      j_rabbitMQService["queueDeclare(java.lang.String,boolean,boolean,boolean,io.vertx.core.Handler)"](queue, durable, exclusive, autoDelete, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
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
      j_rabbitMQService["queueDelete(java.lang.String,io.vertx.core.Handler)"](queue, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
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
      j_rabbitMQService["queueDeleteIf(java.lang.String,boolean,boolean,io.vertx.core.Handler)"](queue, ifUnused, ifEmpty, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
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
      j_rabbitMQService["queueBind(java.lang.String,java.lang.String,java.lang.String,io.vertx.core.Handler)"](queue, exchange, routingKey, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.start = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_rabbitMQService["start(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param resultHandler {function} 
   */
  this.stop = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_rabbitMQService["stop(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_rabbitMQService;
};

/**

 @memberof module:vertx-rabbitmq-js/rabbit_mq_service
 @param vertx {Vertx} 
 @param config {Object} 
 @return {RabbitMQService}
 */
RabbitMQService.create = function(vertx, config) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object') {
    return new RabbitMQService(JRabbitMQService["create(io.vertx.core.Vertx,io.vertx.core.json.JsonObject)"](vertx._jdel, utils.convParamJsonObject(config)));
  } else utils.invalidArgs();
};

/**

 @memberof module:vertx-rabbitmq-js/rabbit_mq_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {RabbitMQService}
 */
RabbitMQService.createEventBusProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return new RabbitMQService(JRabbitMQService["createEventBusProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = RabbitMQService;