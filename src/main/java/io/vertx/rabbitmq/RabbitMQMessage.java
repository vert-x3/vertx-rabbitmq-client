package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * Represent a message received message received in a rabbitmq-queue.
 */
@DataObject(generateConverter = true)
public class RabbitMQMessage {

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
  public RabbitMQMessage(byte[] body, String consumerTag, Envelope envelope, AMQP.BasicProperties properties) {
    this.body = Buffer.buffer(body);
    this.consumerTag = consumerTag;
    this.envelope = envelope;
    this.properties = properties;
  }

  public RabbitMQMessage(){

  }

  public RabbitMQMessage(JsonObject json) {
    this();
    RabbitMQMessageConverter.fromJson(json, this);
  }

  /**
   * @return the message body (opaque, client-specific byte array)
   */
  public Buffer body() {
    return body;
  }

  /**
   * @return the <i>consumer tag</i> associated with the consumer
   */
  public String consumerTag() {
    return consumerTag;
  }

  /**
   * @return packaging data for the message
   */
  public Envelope envelope() {
    return envelope;
  }

  /**
   * @return content header data for the message
   */
  public AMQP.BasicProperties properties() {
    return properties;
  }
}
