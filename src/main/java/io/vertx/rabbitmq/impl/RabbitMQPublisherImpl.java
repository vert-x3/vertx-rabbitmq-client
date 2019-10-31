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
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.impl.InboundBuffer;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConfirmListener;
import io.vertx.rabbitmq.RabbitMQConfirmation;
import io.vertx.rabbitmq.RabbitMQPublisher;
import io.vertx.rabbitmq.RabbitMQPublisher.Confirmation;
import io.vertx.rabbitmq.RabbitMQPublisherOptions;
import io.vertx.rabbitmq.impl.RabbitMQPublisherImpl.MessageDetails;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author jtalbut
 */
public class RabbitMQPublisherImpl implements RabbitMQPublisher, ReadStream<Confirmation> {
  
  private final Vertx vertx;
  private final RabbitMQClient client;
  private final InboundBuffer<Confirmation> confirmations;
  private final Context context;
  private final RabbitMQPublisherOptions options;
  
  private final Deque<MessageDetails> awaitingAck = new ArrayDeque<>();
  private final InboundBuffer<MessageDetails> sendQueue;
  private long lastChannelInstance = 0;
  
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
          , Runnable connectionEstablishedCallback
  ) throws Throwable {
    this.vertx = vertx;
    this.client = client;
    this.context = vertx.getOrCreateContext();
    this.confirmations = new InboundBuffer<>(context);
    this.sendQueue = new InboundBuffer<>(context);
    sendQueue.handler(md -> handleMessageSend(md));
    this.options = options;
    this.client.addConnectionEstablishedCallback(() -> {
      addConfirmListener(client, options, null);
    });
    this.client.addConnectionEstablishedCallback(connectionEstablishedCallback);

    CompletableFuture<Void> latch = new CompletableFuture<>();
    addConfirmListener(client, options, ar -> {
      if (ar.succeeded()) {
        latch.complete(null);
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    try {
      latch.get();
    } catch(ExecutionException ex) {
      throw ex.getCause();
    }
  }

  protected final void addConfirmListener(RabbitMQClient client1
          , RabbitMQPublisherOptions options1
          , Handler<AsyncResult<RabbitMQConfirmListener>> resultHandler
  ) {    
    client1.addConfirmListener(options1.getMaxInternalQueueSize(),
            ar -> {
              if (ar.succeeded()) {
                ar.result().handler(confirmation -> {
                  handleConfirmation(confirmation);
                });
              }
              if (resultHandler != null) {
                resultHandler.handle(ar);
              }
            });
  }

  @Override
  public ReadStream<Confirmation> getConfirmationStream() {
    return this;
  }

  @Override
  public int getQueueSize() {
    return sendQueue.size();
  }
  
  private void handleMessageSend(MessageDetails md) {
    sendQueue.pause();
    synchronized(awaitingAck) {
      awaitingAck.add(md);
    }
    doSend(md);    
  }

  private void doSend(MessageDetails md) {
    try {
      client.basicPublish(md.exchange, md.routingKey, md.properties, md.message
          , dt -> { md.setDeliveryTag(dt); }
          , publishResult -> {
            try {              
              if (publishResult.succeeded()) {
                if (md.publishHandler != null) {
                  md.publishHandler.handle(null);
                }
                sendQueue.resume();
              } else {
                if (md.publishHandler != null) {
                  md.publishHandler.handle(publishResult);
                }
                synchronized(awaitingAck) {
                  awaitingAck.remove(md);
                }
                client.stop(v -> {
                  client.start(v2 -> {
                    doSend(md);
                  });
                });
              }
            } finally {
            }
          });
    } catch(Throwable ex) {
      synchronized(awaitingAck) {
        awaitingAck.remove(md);
      }
      client.stop(v -> {
        client.start(v2 -> {
          doSend(md);
        });
      });
    }
  }
  
  private void handleConfirmation(RabbitMQConfirmation rawConfirmation) {
    synchronized(awaitingAck) {
      if (lastChannelInstance == 0) {
        lastChannelInstance = rawConfirmation.getChannelInstance();
      } else if (lastChannelInstance != rawConfirmation.getChannelInstance()) {
        awaitingAck.clear();
        lastChannelInstance = rawConfirmation.getChannelInstance();
      }
      if (rawConfirmation.isMultiple()) {
        for (Iterator<MessageDetails> iter = awaitingAck.iterator(); iter.hasNext(); ) {
          MessageDetails md = iter.next();
          if (md.deliveryTag <= rawConfirmation.getDeliveryTag()) {
            String messageId = md.properties == null ?  null : md.properties.getMessageId();
            confirmations.write(new Confirmation(messageId, rawConfirmation.isSucceeded()));
            iter.remove();
          }
        }
      } else {
        for (Iterator<MessageDetails> iter = awaitingAck.iterator(); iter.hasNext(); ) {
          MessageDetails md = iter.next();
          if (md.deliveryTag == rawConfirmation.getDeliveryTag()) {
            String messageId = md.properties == null ?  null : md.properties.getMessageId();
            confirmations.write(new Confirmation(messageId, rawConfirmation.isSucceeded()));
            iter.remove();
          }
        }
      }
    }
  }
  
  @Override
  public void publish(String exchange, String routingKey, BasicProperties properties, Buffer body, Handler<AsyncResult<Void>> resultHandler) {
    context.runOnContext(e -> {
      sendQueue.write(new MessageDetails(exchange, routingKey, properties, body, resultHandler));
    });
  }

  @Override
  public Future<Void> publish(String exchange, String routingKey, BasicProperties properties, Buffer body) {
    Promise<Void> promise = Promise.promise();
    publish(exchange, routingKey, properties, body, promise);
    return promise.future();
  }  
  
  @Override
  public RabbitMQPublisherImpl exceptionHandler(Handler<Throwable> hndlr) {
    confirmations.exceptionHandler(hndlr);
    return this;
  }

  @Override
  public RabbitMQPublisherImpl handler(Handler<Confirmation> hndlr) {
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
