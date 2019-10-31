package io.vertx.rabbitmq.impl;

import io.vertx.rabbitmq.RabbitMQConfirmation;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.streams.impl.InboundBuffer;
import io.vertx.rabbitmq.RabbitMQConfirmListener;

/**
 *
 * @author jtalbut
 */
public class RabbitMQConfirmListenerImpl implements RabbitMQConfirmListener {

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
  public RabbitMQConfirmListener exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }
  
  @Override
  public RabbitMQConfirmListener handler(Handler<RabbitMQConfirmation> handler) {
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
  public RabbitMQConfirmListener pause() {
    pending.pause();
    return this;
  }

  @Override
  public RabbitMQConfirmListener resume() {
    pending.resume();
    return this;
  }

  @Override
  public RabbitMQConfirmListener fetch(long amount) {
    pending.fetch(amount);
    return this;
  }

  @Override
  public boolean isPaused() {
    return pending.isPaused();
  }

  @Override
  public RabbitMQConfirmListener endHandler(Handler<Void> hndlr) {
    return this;
  }
  
}
