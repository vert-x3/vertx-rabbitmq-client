package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.ConfirmListener;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import java.io.IOException;

/**
 *
 * @author jtalbut
 */
public class ChannelConfirmHandler implements ConfirmListener {

  private final RabbitMQConfirmListenerImpl listener;
  private final Context handlerContext;

  ChannelConfirmHandler(Vertx vertx, RabbitMQClientImpl client, int maxQueueSize) {
    this.handlerContext = vertx.getOrCreateContext();
    this.listener = new RabbitMQConfirmListenerImpl(client, handlerContext, maxQueueSize);
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
