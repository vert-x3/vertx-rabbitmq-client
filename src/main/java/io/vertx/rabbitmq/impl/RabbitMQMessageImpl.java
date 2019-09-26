package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Envelope;
import io.vertx.core.buffer.Buffer;
import io.vertx.rabbitmq.RabbitMQMessage;

public class RabbitMQMessageImpl implements RabbitMQMessage {

  private Buffer body;
  private String consumerTag;
  private Envelope envelope;
  private BasicProperties properties;
  private Integer messageCount;

  /**
   * Construct a new message
   *
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   * @param envelope    packaging data for the message
   * @param properties  content header data for the message
   * @param body        the message body (opaque, client-specific byte array)
   */
  RabbitMQMessageImpl(byte[] body, String consumerTag, com.rabbitmq.client.Envelope envelope, AMQP.BasicProperties properties, Integer messageCount) {
    this.body = Buffer.buffer(body);
    this.consumerTag = consumerTag;
    this.envelope = envelope;
    this.properties = properties;
    this.messageCount = messageCount;
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

  @Override
  public Integer messageCount() {
    return messageCount;
  }
}
