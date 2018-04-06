package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQMessage;

public class RabbitMQMessageImpl implements RabbitMQMessage {

  private Buffer body;
  private String consumerTag;
  private Envelope envelope;
  private AMQP.BasicProperties properties;

  /**
   * Construct a new message
   *
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   * @param envelope    packaging data for the message
   * @param properties  content header data for the message
   * @param body        the message body (opaque, client-specific byte array)
   */
  public RabbitMQMessageImpl(byte[] body, String consumerTag, Envelope envelope, AMQP.BasicProperties properties) {
    this.body = Buffer.buffer(body);
    this.consumerTag = consumerTag;
    this.envelope = envelope;
    this.properties = properties;
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
    return null;
  }

  @Override
  public AMQP.BasicProperties properties() {
    return null;
  }
}
