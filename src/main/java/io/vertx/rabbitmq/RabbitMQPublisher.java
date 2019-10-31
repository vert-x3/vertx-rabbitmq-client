package io.vertx.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.rabbitmq.impl.RabbitMQPublisherImpl;

/**
 * A reliable publisher that
 * <ul>
 * <li>Queues up messages internally until it can successfully call basicPublish.
 * <li>Notifies the caller using a robust ID (not delivery tag) when the message is confirmed by rabbit.
 * </ul>
 * 
 * This is a layer above the RabbitMQClient that provides a lot of standard implementation when guaranteed at least once delivery is required.
 * If confirmations are not required do not use this publisher as it does have overhead.
 * 
 * @author jtalbut
 */
public interface RabbitMQPublisher {
  
  public class Confirmation {

    private final String messageId;
    private final boolean succeeded;

    public Confirmation(String messageId, boolean succeeded) {
      this.messageId = messageId;
      this.succeeded = succeeded;
    }

    public String getMessageId() {
      return messageId;
    }

    public boolean isSucceeded() {
      return succeeded;
    }

  }
  
  
  /**
   * Create and return a publisher using the specified client.
   *
   * @param client the RabbitMQClient
   * @return the publisher
   */
  static RabbitMQPublisher create(Vertx vertx
          , RabbitMQClient client
          , RabbitMQPublisherOptions options
          , Runnable connectionEstablishedCallback
  ) throws Throwable {
    return new RabbitMQPublisherImpl(vertx, client, options, connectionEstablishedCallback);
  }
  
  /**
   * Get the ReadStream that contains the message IDs for confirmed messages.
   * The message IDs in this ReadStream are taken from the message properties,
   * if these message IDs are not set then this ReadStream will contain nulls and using this publisher will be pointless.
   * 
   * @return the ReadStream that contains the message IDs for confirmed messages.
   */
  ReadStream<Confirmation> getConfirmationStream();

  /**
   * Publish a message. 
   *
   * @see com.rabbitmq.client.Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  void publish(String exchange, String routingKey, BasicProperties properties, Buffer body, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Like {@link #basicPublish(String, String, BasicProperties, Buffer, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Future<Void> publish(String exchange, String routingKey, BasicProperties properties, Buffer body);
  
  /**
   * Get the number of published, but not sent, messages.
   * @return the number of published, but not sent, messages.
   */
  int getQueueSize();
  
}
