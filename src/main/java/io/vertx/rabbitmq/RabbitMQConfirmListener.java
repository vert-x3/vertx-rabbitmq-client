/*
 * Copyright 2019 Eclipse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.rabbitmq;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

/**
 *
 * @author jtalbut
 */
@VertxGen
public interface RabbitMQConfirmListener extends ReadStream<RabbitMQConfirmation> {
  
  /**
   * Set an exception handler on the read stream.
   *
   * @param exceptionHandler the exception handler
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConfirmListener exceptionHandler(Handler<Throwable> exceptionHandler);

  /**
   * Set a message handler. As message appear in a queue, the handler will be called with the message.
   *
   * @param messageConfirmationHandler the handler for message confirmations
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConfirmListener handler(Handler<RabbitMQConfirmation> messageConfirmationHandler);

  /**
   * Pause the stream of incoming messages from queue.
   * <p>
   * The messages will continue to arrive, but they will be stored in a internal queue.
   * If the queue size would exceed the limit provided by {@link RabbitMQConsumer#size(int)}, then incoming messages will be discarded.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConfirmListener pause();

  /**
   * Resume reading from a queue. Flushes internal queue.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  RabbitMQConfirmListener resume();

  /**
   * Set an end handler on the read stream.
   *
   * @param endHandler the end handler
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  public RabbitMQConfirmListener endHandler(Handler<Void> endHandler);

  /**
   * Fetch the specified amount of elements. 
   * If the ReadStream has been paused, reading will recommence with the specified amount of items, otherwise the specified amount will be added to the current stream demand.
   *
   * @param count the number of items
   * @return a reference to this, so the API can be used fluently
   */
  @Override
  public RabbitMQConfirmListener fetch(long count);

  
  /**
   * @return is the stream paused?
   */
  boolean isPaused();
  
  
  
}
