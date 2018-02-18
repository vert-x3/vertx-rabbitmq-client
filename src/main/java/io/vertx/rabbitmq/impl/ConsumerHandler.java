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

import static io.vertx.rabbitmq.impl.Utils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class ConsumerHandler extends DefaultConsumer {

  private final Vertx vertx;
  private final RabbitMQueueImpl queue;
  private final boolean includeProperties;
  private final Context handlerContext;

  private static final Logger log = LoggerFactory.getLogger(ConsumerHandler.class);

  ConsumerHandler(Vertx vertx, Channel channel, boolean includeProperties, RabbitMQueueImpl queue) {
    super(channel);
    this.handlerContext = vertx.getOrCreateContext();
    this.vertx = vertx;
    this.includeProperties = includeProperties;
    this.queue = queue;
  }

  // TODO: Think about implementing all Consume methods and deliver that back to the handler ?

  @Override
  //TODO: Clean this up, some dup logic here and in basicGet of RabbitMQServiceImpl
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
    JsonObject msg = new JsonObject();
    msg.put("consumerTag", consumerTag);

    // Add the envelope data
    populate(msg, envelope);

    // Add properties (if configured)
    if (includeProperties) {
      put("properties", toJson(properties), msg);
    }

    //TODO: Allow an SPI which can be pluggable to handle parsing the body
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
}
