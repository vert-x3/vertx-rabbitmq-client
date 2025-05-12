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
package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.BasicProperties;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.concurrent.InboundMessageQueue;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConfirmation;
import io.vertx.rabbitmq.RabbitMQPublisher;
import io.vertx.rabbitmq.RabbitMQPublisherConfirmation;
import io.vertx.rabbitmq.RabbitMQPublisherOptions;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author jtalbut
 */
public class RabbitMQPublisherImpl implements RabbitMQPublisher, ReadStream<RabbitMQPublisherConfirmation> {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisherImpl.class);

  private final RabbitMQClient client;
  private final ConfirmationQueue confirmationQueue;
  private final ContextInternal context;
  private final RabbitMQPublisherOptions options;

  private final Deque<MessageDetails> pendingAcks = new ArrayDeque<>();
  private final SendQueue sendQueue;
  private long lastChannelInstance = 0;
  private volatile boolean stopped = false;

  private class ConfirmationQueue extends InboundMessageQueue<RabbitMQPublisherConfirmation> {

    private Handler<RabbitMQPublisherConfirmation> handler;

    public ConfirmationQueue(ContextInternal context) {
      super(context.executor(), context.executor());
    }

    @Override
    protected void handleMessage(RabbitMQPublisherConfirmation msg) {
      Handler<RabbitMQPublisherConfirmation> h = handler;
      if (h != null) {
        context.dispatch(msg, h);
      }
    }
    void enqueue(RabbitMQPublisherConfirmation confirmation) {
      context.execute(confirmation, t -> write(confirmation));
    }
  }

  private class SendQueue extends InboundMessageQueue<Object> {

    private int size;
    private final AtomicInteger volatileSize = new AtomicInteger();

    public SendQueue(ContextInternal context) {
      super(context.executor(), context.executor());
    }
    @Override
    protected void handleMessage(Object msg) {
      if (msg instanceof MessageDetails) {
        volatileSize.setRelease(--size);
        MessageDetails md = (MessageDetails) msg;
        handleMessageSend(md);
      } else if (msg instanceof Promise) {
        Promise<?> promise = (Promise<?>) msg;
        promise.complete();
      }
    }
    void enqueue(MessageDetails msg) {
      volatileSize.setRelease(++size);
      context.execute(msg, this::write);
    }
    int size() {
      return volatileSize.get();
    }
    public void checkpoint(Promise<Void> promise) {
      context.execute(promise, this::write);
    }
  }

  /**
   * POD for holding message details pending acknowledgement.
   */
  private static class MessageDetails {

    private final String exchange;
    private final String routingKey;
    private final BasicProperties properties;
    private final Buffer message;
    private final Promise<Void> publishHandler;
    private final Promise<Long> confirmHandler;
    private volatile long deliveryTag;

    MessageDetails(String exchange, String routingKey, BasicProperties properties, Buffer message,
                   Promise<Void> publishHandler,
                   Promise<Long> confirmHandler) {
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.properties = properties;
      this.message = message;
      this.publishHandler = publishHandler;
      this.confirmHandler = confirmHandler;
    }

    public void setDeliveryTag(long deliveryTag) {
      this.deliveryTag = deliveryTag;
    }

  }

  public RabbitMQPublisherImpl(Vertx vertx, RabbitMQClient client , RabbitMQPublisherOptions options) {
    this.client = client;
    this.context = (ContextInternal) vertx.getOrCreateContext();
    this.confirmationQueue = new ConfirmationQueue(context);
    this.sendQueue = new SendQueue(context);
    this.options = options;
    this.client.addConnectionEstablishedCallback(p -> {
      addConfirmListener(client, options, p);
      if(client instanceof RabbitMQClientImpl){
        if (lastChannelInstance == 0) {
          lastChannelInstance = ((RabbitMQClientImpl)client).getChannelInstance();
        } else if (lastChannelInstance != ((RabbitMQClientImpl)client).getChannelInstance()) {
          pendingAcks.clear();
          lastChannelInstance = ((RabbitMQClientImpl)client).getChannelInstance();
        }
      }
    });
  }

  @Override
  public Future<Void> start() {
    Promise<Void> promise = startForPromise();
    return promise.future();
  }

  private void stop(Promise<Void> resultHandler) {
    stopped = true;
    sendQueue.checkpoint(resultHandler);
  }

  @Override
  public Future<Void> stop() {
    Promise<Void> promise = Promise.promise();
    stop(promise);
    return promise.future();
  }

  @Override
  public void restart() {
    stopped = false;
    sendQueue.fetch(Long.MAX_VALUE);
  }

  private Promise<Void> startForPromise() {
    Promise<Void> promise = Promise.promise();
    addConfirmListener(client, options, promise);
    return promise;
  }

  protected final void addConfirmListener(RabbitMQClient client1
          , RabbitMQPublisherOptions options1
          , Promise<Void> promise
  ) {
    context.runOnContext(unused -> {
      client1.addConfirmListener(options1.getMaxInternalQueueSize()).onComplete(
        ar -> {
          if (ar.succeeded()) {
            ar.result().handler(confirmation -> {
              handleConfirmation(confirmation);
            });
            promise.complete();
          } else {
            log.error("Failed to add confirmListener: ", ar.cause());
            promise.fail(ar.cause());
          }
        });
    });
  }

  @Override
  public ReadStream<RabbitMQPublisherConfirmation> getConfirmationStream() {
    return this;
  }

  @Override
  public int queueSize() {
    return sendQueue.size();
  }

  private void handleMessageSend(MessageDetails md) {
    sendQueue.pause();
    synchronized(pendingAcks) {
      pendingAcks.add(md);
    }
    doSend(md);
  }

  private void doSend(MessageDetails md) {
    try {
      client.basicPublishWithDeliveryTag(md.exchange, md.routingKey, md.properties, md.message
          , dt -> { md.setDeliveryTag(dt); }).onComplete(
          publishResult -> {
            if (publishResult.succeeded()) {
              if (md.publishHandler != null) {
                try {
                  md.publishHandler.handle(publishResult);
                } catch(Throwable ex) {
                  log.warn("Failed to handle publish result", ex);
                }
              }
              sendQueue.fetch(Long.MAX_VALUE);
            } else {
              log.info("Failed to publish message: " + publishResult.cause().toString());
              synchronized(pendingAcks) {
                pendingAcks.remove(md);
              }
              client.restartConnect(0).onComplete(rcRt->{
                doSend(md);
              });
            }
          });
    } catch(Throwable ex) {
      synchronized(pendingAcks) {
        pendingAcks.remove(md);
      }
      client.restartConnect(0).onComplete(rcRt->{
        doSend(md);
      });
    }
  }

  private void handleConfirmation(RabbitMQConfirmation rawConfirmation) {
    synchronized(pendingAcks) {
      if (rawConfirmation.isMultiple()) {
        for (Iterator<MessageDetails> iter = pendingAcks.iterator(); iter.hasNext(); ) {
          MessageDetails md = iter.next();
          if (md.deliveryTag <= rawConfirmation.getDeliveryTag()) {
            String messageId = md.properties == null ?  null : md.properties.getMessageId();
            confirmationQueue.enqueue(new RabbitMQPublisherConfirmation(messageId, rawConfirmation.getDeliveryTag(), rawConfirmation.isSucceeded()));
            if (md.confirmHandler != null) {
              try {
                md.confirmHandler.handle(rawConfirmation.isSucceeded() ? Future.succeededFuture(md.deliveryTag) :
                  Future.failedFuture("Message publish nacked by the broker"));
              } catch (Throwable ex) {
                log.warn("Failed to handle publish confirm", ex);
              }
            }
            iter.remove();
          } else {
            break ;
          }
        }
      } else {
        for (Iterator<MessageDetails> iter = pendingAcks.iterator(); iter.hasNext(); ) {
          MessageDetails md = iter.next();
          if (md.deliveryTag == rawConfirmation.getDeliveryTag()) {
            String messageId = md.properties == null ?  null : md.properties.getMessageId();
            confirmationQueue.enqueue(new RabbitMQPublisherConfirmation(messageId, rawConfirmation.getDeliveryTag(), rawConfirmation.isSucceeded()));
            if (md.confirmHandler != null) {
              try {
                md.confirmHandler.handle(rawConfirmation.isSucceeded() ? Future.succeededFuture(md.deliveryTag) :
                  Future.failedFuture("Message publish nacked by the broker"));
              } catch (Throwable ex) {
                log.warn("Failed to handle publish confirm", ex);
              }
            }
            iter.remove();
            break ;
          }
        }
      }
    }
  }

  @Override
  public Future<Void> publish(String exchange, String routingKey, BasicProperties properties, Buffer body) {
    Promise<Void> promise = Promise.promise();
    if (!stopped) {
      sendQueue.enqueue(new MessageDetails(exchange, routingKey, properties, body, promise, null));
    }
    return promise.future();
  }

  @Override
  public Future<Long> publishConfirm(String exchange, String routingKey, BasicProperties properties, Buffer body) {
    Promise<Long> promise = Promise.promise();
    if (!stopped) {
      sendQueue.enqueue(new MessageDetails(exchange, routingKey, properties, body, null, promise));
    }
    return promise.future();
  }

  @Override
  public RabbitMQPublisherImpl exceptionHandler(Handler<Throwable> hndlr) {
    return this;
  }

  @Override
  public RabbitMQPublisherImpl handler(Handler<RabbitMQPublisherConfirmation> hndlr) {
    confirmationQueue.handler = hndlr;
    return this;
  }

  @Override
  public RabbitMQPublisherImpl pause() {
    confirmationQueue.pause();
    return this;
  }

  @Override
  public RabbitMQPublisherImpl resume() {
    confirmationQueue.fetch(Long.MAX_VALUE);
    return this;
  }

  @Override
  public RabbitMQPublisherImpl fetch(long l) {
    confirmationQueue.fetch(l);
    return this;
  }

  @Override
  public RabbitMQPublisherImpl endHandler(Handler<Void> hndlr) {
    return this;
  }
}
