package io.vertx.rabbitmq.impl;

import io.vertx.core.*;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.concurrent.InboundMessageQueue;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;

import java.io.IOException;

/**
 * A implementation of {@link RabbitMQConsumer}
 */
public class RabbitMQConsumerImpl implements RabbitMQConsumer {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumerImpl.class);

  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;
  private String queueName;
  private final QueueConsumerHandler consumerHandler;
  private final PendingQueue pending;
  private final int maxQueueSize;
  private volatile boolean cancelled;

  class PendingQueue extends InboundMessageQueue<RabbitMQMessage> {

    private final ContextInternal context;
    private Handler<RabbitMQMessage> handler;
    private int size;

    public PendingQueue(ContextInternal context) {
      super(context.executor(), context.executor());
      this.context = context;
    }

    @Override
    protected void handleMessage(RabbitMQMessage msg) {
      size--;
      Handler<RabbitMQMessage> h = handler;
      if (h != null) {
        context.dispatch(msg, m -> {
          try {
            h.handle(m);
          } catch (Exception e) {
            handleException(e);
          }
        });
      }
    }

    /**
     * Push message to stream.
     * <p>
     * Should be called from a vertx thread.
     *
     * @param message received message to deliver
     */
    void enqueue(RabbitMQMessage message) {
      context.execute(message, m -> {
        if (size++ < maxQueueSize) {
          write(m);
        }
      });
    }
  }

  RabbitMQConsumerImpl(Context context, QueueConsumerHandler consumerHandler, QueueOptions options, String queueName) {

    PendingQueue pending = new PendingQueue((ContextInternal) context);
    pending.pause();

    this.consumerHandler = consumerHandler;
    this.maxQueueSize = options.maxInternalQueueSize();
    this.pending = pending;
    this.queueName = queueName;
  }

  @Override
  public String queueName() {
    return queueName;
  }

  @Override
  public RabbitMQConsumer setQueueName(String name) {
    this.queueName = name;
    return this;
  }

  @Override
  public RabbitMQConsumer exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  @Override
  public RabbitMQConsumer handler(Handler<RabbitMQMessage> handler) {
    pending.handler = handler;
    return this;
  }

  @Override
  public RabbitMQConsumer pause() {
    pending.pause();
    return this;
  }

  @Override
  public RabbitMQConsumer resume() {
    pending.fetch(Long.MAX_VALUE);
    return this;
  }

  @Override
  public RabbitMQConsumer fetch(long amount) {
    pending.fetch(amount);
    return this;
  }

  @Override
  public RabbitMQConsumer endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public String consumerTag() {
    return consumerHandler.getConsumerTag();
  }

  @Override
  public Future<Void> cancel() {
    Future<Void> operationResult;
    try {
      log.debug("Cancelling " + consumerTag());
      cancelled = true;
      consumerHandler.getChannel().basicCancel(consumerTag());
      operationResult = Future.succeededFuture();
    } catch (IOException e) {
      operationResult = Future.failedFuture(e);
    }
    handleEnd();
    return operationResult;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public boolean isPaused() {
    return false;
  }

  /**
   * Push message to stream.
   * <p>
   * Should be called from a vertx thread.
   *
   * @param message received message to deliver
   */
  void handleMessage(RabbitMQMessage message) {
    pending.enqueue(message);
  }

  /**
   * Trigger exception handler with given exception
   */
  private void handleException(Throwable exception) {
    Handler<Throwable> h = exceptionHandler;
    if (h != null) {
      h.handle(exception);
    }
  }

  /**
   * Trigger end of stream handler
   */
  void handleEnd() {
    Handler<Void> h = endHandler;
    if (h != null) {
      h.handle(null);
    }
  }
}
