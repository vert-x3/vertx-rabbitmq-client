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
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.impl.InboundBuffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConfirmation;
import io.vertx.rabbitmq.RabbitMQPublisher;
import io.vertx.rabbitmq.RabbitMQPublisherConfirmation;
import io.vertx.rabbitmq.RabbitMQPublisherOptions;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 *
 * @author jtalbut
 */
public class RabbitMQPublisherImpl implements RabbitMQPublisher, ReadStream<RabbitMQPublisherConfirmation> {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisherImpl.class);

  private final Vertx vertx;
  private final RabbitMQClient client;
  private final InboundBuffer<RabbitMQPublisherConfirmation> confirmations;
  private final Context context;
  private final RabbitMQPublisherOptions options;

  private final Deque<MessageDetails> pendingAcks = new ArrayDeque<>();
  private final InboundBuffer<MessageDetails> sendQueue;
  private long lastChannelInstance = 0;
  private volatile boolean stopped = false;

  /**
   * POD for holding message details pending acknowledgement.
   * @param <I> The type of the message IDs.
   */
  static class MessageDetails {

    private final String exchange;
    private final String routingKey;
    private final BasicProperties properties;
    private final Buffer message;
    private final Handler<AsyncResult<Void>> publishHandler;
    private volatile long deliveryTag;

    MessageDetails(String exchange, String routingKey, BasicProperties properties, Buffer message, Handler<AsyncResult<Void>> publishHandler) {
      this.exchange = exchange;
      this.routingKey = routingKey;
      this.properties = properties;
      this.message = message;
      this.publishHandler = publishHandler;
    }

    public void setDeliveryTag(long deliveryTag) {
      this.deliveryTag = deliveryTag;
    }

  }

  public RabbitMQPublisherImpl(Vertx vertx
          , RabbitMQClient client
          , RabbitMQPublisherOptions options
  ) {
    this.vertx = vertx;
    this.client = client;
    this.context = vertx.getOrCreateContext();
    this.confirmations = new InboundBuffer<>(context);
    this.sendQueue = new InboundBuffer<>(context);
    sendQueue.handler(md -> handleMessageSend(md));
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

  private void stop(Handler<AsyncResult<Void>> resultHandler) {
    stopped = true;
    sendQueue.pause();
    if (sendQueue.isEmpty()) {
      resultHandler.handle(Future.succeededFuture());
    } else {
      sendQueue.emptyHandler(v -> {
        resultHandler.handle(Future.succeededFuture());
      });
    }
    sendQueue.resume();
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
    sendQueue.pause();
    sendQueue.emptyHandler(null);
    sendQueue.resume();
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
              sendQueue.resume();
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
            confirmations.write(new RabbitMQPublisherConfirmation(messageId, rawConfirmation.isSucceeded()));
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
            confirmations.write(new RabbitMQPublisherConfirmation(messageId, rawConfirmation.isSucceeded()));
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
      context.runOnContext(e -> {
        sendQueue.write(new MessageDetails(exchange, routingKey, properties, body, promise));
      });
    }
    return promise.future();
  }

  @Override
  public RabbitMQPublisherImpl exceptionHandler(Handler<Throwable> hndlr) {
    confirmations.exceptionHandler(hndlr);
    return this;
  }

  @Override
  public RabbitMQPublisherImpl handler(Handler<RabbitMQPublisherConfirmation> hndlr) {
    confirmations.handler(hndlr);
    return this;
  }

  @Override
  public RabbitMQPublisherImpl pause() {
    confirmations.pause();
    return this;
  }

  @Override
  public RabbitMQPublisherImpl resume() {
    confirmations.resume();
    return this;
  }

  @Override
  public RabbitMQPublisherImpl fetch(long l) {
    confirmations.fetch(l);
    return this;
  }

  @Override
  public RabbitMQPublisherImpl endHandler(Handler<Void> hndlr) {
    return this;
  }



}
