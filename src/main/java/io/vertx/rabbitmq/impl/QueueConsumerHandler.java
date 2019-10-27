package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;


public class QueueConsumerHandler extends DefaultConsumer {

  private final RabbitMQConsumerImpl queue;
  private final Context handlerContext;
  private Handler<ShutdownSignalException> shutdownHandler;

  private static final Logger log = LoggerFactory.getLogger(QueueConsumerHandler.class);

  QueueConsumerHandler(Vertx vertx, Channel channel, QueueOptions options, String queueName) {
    super(channel);
    this.handlerContext = vertx.getOrCreateContext();
    this.queue = new RabbitMQConsumerImpl(handlerContext, this, options, queueName);
  }

  public void setShutdownHandler(Handler<ShutdownSignalException> shutdownHandler) {
    this.shutdownHandler = shutdownHandler;
  }

  public void setShutdownHandler(Handler<ShutdownSignalException> shutdownHandler) {
    this.shutdownHandler = shutdownHandler;
  }

  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
    RabbitMQMessage msg = new RabbitMQMessageImpl(body, consumerTag, envelope, properties, null);
    this.handlerContext.runOnContext(v -> queue.handleMessage(msg));
  }

  @Override
  public void handleConsumeOk(String consumerTag) {
    super.handleConsumeOk(consumerTag);
    log.debug("Consumer tag is now " + consumerTag);
  }

  @Override
  public void handleCancel(String consumerTag) {
    log.debug("consumer has been cancelled unexpectedly: " + consumerTag);
    queue.handleEnd();
  }

  @Override
  public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
    log.debug("consumer has been shutdown unexpectedly: " + consumerTag);
    if ((this.shutdownHandler != null) && !queue.isCancelled()) {
      shutdownHandler.handle(sig);
    }
  }
  
  

  /**
   * @return a queue for message consumption
   */
  public RabbitMQConsumer queue() {
    return queue;
  }
  
}
