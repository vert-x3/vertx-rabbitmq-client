package io.vertx.rabbitmq.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A implementation of {@link RabbitMQConsumer}
 */
public class RabbitMQConsumerImpl implements RabbitMQConsumer {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumerImpl.class);

  private Handler<Throwable> exceptionHandler;
  private Handler<RabbitMQMessage> messageArrivedHandler;
  private Handler<Void> endHandler;
  private final QueueConsumerHandler consumerHandler;
  private final Context runningContext;
  private final Lock queueRemoveLock = new ReentrantLock();
  private final boolean buffer;
  private final boolean keepMostRecent;

  private volatile int queueSize;
  private final AtomicInteger currentQueueSize = new AtomicInteger(0);
  private final AtomicBoolean paused = new AtomicBoolean(false);

  // a storage of all received messages
  private Queue<RabbitMQMessage> messagesQueue = new ConcurrentLinkedQueue<>();

  RabbitMQConsumerImpl(Vertx vertx, QueueConsumerHandler consumerHandler, QueueOptions options) {
    runningContext = vertx.getOrCreateContext();
    this.consumerHandler = consumerHandler;
    this.keepMostRecent = options.isKeepMostRecent();
    this.buffer = options.isBuffer();
    this.queueSize = options.maxInternalQueueSize();
  }

  @Override
  public RabbitMQConsumer exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    return this;
  }

  @Override
  public RabbitMQConsumer handler(Handler<RabbitMQMessage> messageArrivedHandler) {
    this.messageArrivedHandler = messageArrivedHandler;
    flushQueue();
    return this;
  }

  @Override
  public RabbitMQConsumer pause() {
    paused.set(true);
    return this;
  }

  @Override
  public RabbitMQConsumer resume() {
    paused.set(false);
    flushQueue();
    return this;
  }

  @Override
  public RabbitMQConsumer endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public synchronized void size(int value) {
    queueSize = value;
  }

  @Override
  public String consumerTag() {
    return consumerHandler.getConsumerTag();
  }

  @Override
  public void cancel() {
    cancel(null);
  }

  @Override
  public void cancel(Handler<AsyncResult<Void>> cancelResult) {
    AsyncResult<Void> operationResult;
    try {
      consumerHandler.getChannel().basicCancel(consumerTag());
      operationResult = Future.succeededFuture();
    } catch (IOException e) {
      operationResult = Future.failedFuture(e);
    }
    if (cancelResult != null) {
      cancelResult.handle(operationResult);
    }
    triggerStreamEnd();
  }

  @Override
  public boolean isPaused() {
    return paused.get();
  }

  /**
   * Push message to stream.
   * <p>
   * Should be called from a vertx thread.
   *
   * @param message received message to deliver
   */
  void push(RabbitMQMessage message) {

    if (paused.get() && !buffer) {
      log.debug("Discard a received message since stream is paused and buffer flag is false");
      return;
    }

    int expected;
    boolean compareAndSetLoopFlag;
    do {
      expected = currentQueueSize.get();
      if (expected + 1 < queueSize) {
        boolean compareAndSetOp = currentQueueSize.compareAndSet(expected, expected + 1);
        if (compareAndSetOp) {
          messagesQueue.add(message);
        }
        // if compare and set == false then continue CompareAndSet loop
        compareAndSetLoopFlag = !compareAndSetOp;
      } else {
        if (keepMostRecent) {
          queueRemoveLock.lock();
          messagesQueue.poll();
          messagesQueue.add(message);
          queueRemoveLock.unlock();
          log.debug("Remove a old message and put a new message into the internal queue.");
        } else {
          log.debug("Discard a received message due to exceed queue size limit.");
        }

        compareAndSetLoopFlag = false;
      }
    } while (compareAndSetLoopFlag);
    flushQueue();
  }

  /**
   * Trigger exception handler with given exception
   */
  void raiseException(Throwable exception) {
    if (exceptionHandler != null) {
      exceptionHandler.handle(exception);
    }
  }

  /**
   * Trigger end of stream handler
   */
  void triggerStreamEnd() {
    if (endHandler != null) {
      endHandler.handle(null);
    }
  }

  /**
   * Handle all messages in the queue
   */
  private void flushQueue() {
    if (!paused.get()) {
      queueRemoveLock.lock();
      RabbitMQMessage message;
      while ((message = messagesQueue.poll()) != null) {
        if (messageArrivedHandler != null) {
          RabbitMQMessage finalMessage = message;
          runningContext.runOnContext(v -> {
            try {
              messageArrivedHandler.handle(finalMessage);
            } catch (Exception e) {
              raiseException(e);
            }
          });
        }
      }
      currentQueueSize.set(messagesQueue.size());
      queueRemoveLock.unlock();
    }
  }
}
