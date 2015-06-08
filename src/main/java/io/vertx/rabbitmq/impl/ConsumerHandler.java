package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static io.vertx.rabbitmq.impl.Utils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class ConsumerHandler extends DefaultConsumer {

  private final Vertx vertx;
  private final Handler<AsyncResult<JsonObject>> handler;
  private final boolean includeProperties;
  private final boolean autoAck;

  public ConsumerHandler(Vertx vertx, Channel channel, boolean includeProperties, boolean autoAck, Handler<AsyncResult<JsonObject>> handler) {
    super(channel);
    this.vertx = vertx;
    this.includeProperties = includeProperties;
    this.autoAck = autoAck;
    this.handler = handler;
  }

  // TODO: Think about implementing all Consume methods and deliver that back to the handler ?

  @Override
  //TODO: Clean this up, some dup logic here and in basicGet of RabbitMQServiceImpl
  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
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
      vertx.runOnContext(v -> {
        handler.handle(Future.succeededFuture(msg));
      });

      if(autoAck) {
        getChannel().basicAck(envelope.getDeliveryTag(), false);
      } else {
        msg.put("deliveryTag", envelope.getDeliveryTag());
      }
    } catch (UnsupportedEncodingException e) {
      vertx.runOnContext(v -> {
        handler.handle(Future.failedFuture(e));
      });
    }
  }
}
