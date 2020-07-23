package io.vertx.rabbitmq.impl;

import io.vertx.rabbitmq.RabbitMQConfirmation;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.impl.InboundBuffer;

/**
 *
 * @author jtalbut
 */
public class RabbitMQConfirmListenerImpl implements ReadStream<RabbitMQConfirmation> {

  private final RabbitMQClientImpl client;
  private final InboundBuffer<RabbitMQConfirmation> pending;
  private final int maxQueueSize;

  private Handler<Throwable> exceptionHandler;
  
  
  public RabbitMQConfirmListenerImpl(RabbitMQClientImpl client, Context context, int maxQueueSize) {
    this.client = client;
    this.maxQueueSize = maxQueueSize;
    this.pending = new InboundBuffer<>(context, maxQueueSize);
  }

  void handleAck(long deliveryTag, boolean multiple, boolean succeeded) {

    if (pending.size() >= maxQueueSize) {
      pending.read();
    }
    pending.write(new RabbitMQConfirmation(client.getChannelInstance(), deliveryTag, multiple, succeeded));
  }  

  @Override
  public RabbitMQConfirmListenerImpl exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }
  
  @Override
  public RabbitMQConfirmListenerImpl handler(Handler<RabbitMQConfirmation> handler) {
    if (handler != null) {
      pending.handler(msg -> {
        try {
          handler.handle(msg);
        } catch (Exception e) {
          handleException(e);
        }
      });
    } else {
      pending.handler(null);
    }
    return this;
  }

  /**
   * Trigger exception handler with given exception
   */
  private void handleException(Throwable exception) {
    if (exceptionHandler != null) {
      exceptionHandler.handle(exception);
    }
  }
  
  @Override
  public RabbitMQConfirmListenerImpl pause() {
    pending.pause();
    return this;
  }

  @Override
  public RabbitMQConfirmListenerImpl resume() {
    pending.resume();
    return this;
  }

  @Override
  public RabbitMQConfirmListenerImpl fetch(long amount) {
    pending.fetch(amount);
    return this;
  }

  @Override
  public RabbitMQConfirmListenerImpl endHandler(Handler<Void> hndlr) {
    return this;
  }
  
}
