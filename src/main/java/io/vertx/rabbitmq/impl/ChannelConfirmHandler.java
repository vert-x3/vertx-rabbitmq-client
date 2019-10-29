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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.rabbitmq.QueueOptions;
import java.io.IOException;

/**
 *
 * @author jtalbut
 */
public class ChannelConfirmHandler implements ConfirmListener {

  private final RabbitMQConfirmListenerImpl listener;
  private final Context handlerContext;

  ChannelConfirmHandler(Vertx vertx, RabbitMQClientImpl client, QueueOptions options) {
    this.handlerContext = vertx.getOrCreateContext();
    this.listener = new RabbitMQConfirmListenerImpl(client, handlerContext, options);
  }
  
  @Override
  public void handleAck(long deliveryTag, boolean multiple) throws IOException {
    this.handlerContext.runOnContext(v -> listener.handleAck(deliveryTag, multiple, true));
  }

  @Override
  public void handleNack(long deliveryTag, boolean multiple) throws IOException {
    this.handlerContext.runOnContext(v -> listener.handleAck(deliveryTag, multiple, false));
  }

  public RabbitMQConfirmListenerImpl getListener() {
    return listener;
  }

}
