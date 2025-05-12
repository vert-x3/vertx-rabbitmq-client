package io.vertx.rabbitmq.impl;

import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.EventExecutor;
import io.vertx.core.internal.concurrent.InboundMessageQueue;
import io.vertx.rabbitmq.RabbitMQConfirmation;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;

/**
 *
 * @author jtalbut
 */
public class RabbitMQConfirmListenerImpl implements ReadStream<RabbitMQConfirmation> {

  private final RabbitMQClientImpl client;
  private final ConfirmationQueue pending;
  private final int maxQueueSize;

  private Handler<Throwable> exceptionHandler;

  class ConfirmationQueue extends InboundMessageQueue<RabbitMQConfirmation> {

    private final ContextInternal context;
    private Handler<RabbitMQConfirmation> handler;
    private int size;

    public ConfirmationQueue(ContextInternal context) {
      super(context.executor(), context.executor());
      this.context = context;
    }

    @Override
    protected void handleMessage(RabbitMQConfirmation msg) {
      size--;
      context.dispatch(msg, m -> {
        Handler<RabbitMQConfirmation> h = handler;
        if (h != null) {
          try {
            h.handle(m);
          } catch (Exception e) {
            handleException(e);
          }
        }
      });
    }

    void handleAck(long deliveryTag, boolean multiple, boolean succeeded) {
      context.execute(new RabbitMQConfirmation(client.getChannelInstance(), deliveryTag, multiple, succeeded), m -> {
        if (size++ < maxQueueSize) {
          write(m);
        }
      });
    }
  }


  public RabbitMQConfirmListenerImpl(RabbitMQClientImpl client, Context context, int maxQueueSize) {
    this.client = client;
    this.maxQueueSize = maxQueueSize;
    this.pending = new ConfirmationQueue((ContextInternal) context);
  }

  void handleAck(long deliveryTag, boolean multiple, boolean succeeded) {
    pending.handleAck(deliveryTag, multiple, succeeded);
  }

  @Override
  public RabbitMQConfirmListenerImpl exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  @Override
  public RabbitMQConfirmListenerImpl handler(Handler<RabbitMQConfirmation> handler) {
    pending.handler = handler;
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
    pending.fetch(Long.MAX_VALUE);
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
