package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.AMQP;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.BasicProperties;
import io.vertx.rabbitmq.Envelope;
import io.vertx.rabbitmq.RabbitMQMessage;

public class RabbitMQMessageImpl implements RabbitMQMessage {

  private Buffer body;
  private String consumerTag;
  private Envelope envelope;
  private BasicProperties properties;

  /**
   * Construct a new message
   *
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   * @param envelope    packaging data for the message
   * @param properties  content header data for the message
   * @param body        the message body (opaque, client-specific byte array)
   */
  RabbitMQMessageImpl(byte[] body, String consumerTag, com.rabbitmq.client.Envelope envelope, AMQP.BasicProperties properties) {
    this.body = Buffer.buffer(body);
    this.consumerTag = consumerTag;
    this.envelope = new EnvelopeImpl(envelope.getDeliveryTag(), envelope.isRedeliver(), envelope.getExchange(), envelope.getRoutingKey());
    this.properties = new BasicPropertiesImpl(
      properties.getContentType(),
      properties.getContentEncoding(),
      properties.getHeaders(),
      properties.getDeliveryMode(),
      properties.getPriority(),
      properties.getCorrelationId(),
      properties.getReplyTo(),
      properties.getExpiration(),
      properties.getMessageId(),
      properties.getTimestamp(),
      properties.getType(),
      properties.getUserId(),
      properties.getAppId(),
      properties.getClusterId()
    );
  }

  @Override
  public Buffer body() {
    return body;
  }

  @Override
  public String consumerTag() {
    return consumerTag;
  }

  @Override
  public Envelope envelope() {
    return envelope;
  }

  @Override
  public BasicProperties properties() {
    return properties;
  }
}
