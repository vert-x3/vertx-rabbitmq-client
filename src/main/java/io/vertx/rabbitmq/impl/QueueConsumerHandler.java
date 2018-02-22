package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rabbitmq.RabbitMQueue;

import static io.vertx.rabbitmq.impl.Utils.parse;
import static io.vertx.rabbitmq.impl.Utils.populate;
import static io.vertx.rabbitmq.impl.Utils.put;
import static io.vertx.rabbitmq.impl.Utils.toJson;

public class QueueConsumerHandler extends DefaultConsumer {

  private final RabbitMQueueImpl queue;
  private final boolean includeProperties;
  private final Context handlerContext;

  private static final Logger log = LoggerFactory.getLogger(ConsumerHandler.class);

  QueueConsumerHandler(Vertx vertx, Channel channel, boolean includeProperties) {
    super(channel);
    this.handlerContext = vertx.getOrCreateContext();
    this.includeProperties = includeProperties;
    this.queue = new RabbitMQueueImpl(this);
  }

  @Override
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
    JsonObject msg = new JsonObject();
    msg.put("consumerTag", consumerTag);

    // Add the envelope data
    populate(msg, envelope);

    // Add properties (if configured)
    if (includeProperties) {
      put("properties", toJson(properties), msg);
    }

    // Parse the body
    try {
      msg.put("body", parse(properties, body));
      msg.put("deliveryTag", envelope.getDeliveryTag());
      this.handlerContext.runOnContext(v -> queue.push(msg));

    } catch (Exception e) {
      this.handlerContext.runOnContext(v -> queue.raiseException(e));
    }
  }

  @Override
  public void handleCancel(String consumerTag) {
    log.debug("consumer has been cancelled unexpectedly");
    queue.triggerStreamEnd();
  }

  /**
   * @return a queue for message consumption
   */
  public RabbitMQueue queue() {
    return queue;
  }
}
