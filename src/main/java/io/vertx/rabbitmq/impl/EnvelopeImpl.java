package io.vertx.rabbitmq.impl;

import io.vertx.rabbitmq.Envelope;

public class EnvelopeImpl implements Envelope {

  private final long deliveryTag;
  private final boolean redeliver;
  private final String exchange;
  private final String routingKey;

  public EnvelopeImpl(long deliveryTag, boolean redeliver, String exchange, String routingKey) {
    this.deliveryTag = deliveryTag;
    this.redeliver = redeliver;
    this.exchange = exchange;
    this.routingKey = routingKey;
  }

  @Override
  public long deliveryTag() {
    return deliveryTag;
  }

  @Override
  public boolean isRedelivery() {
    return redeliver;
  }

  @Override
  public String exchange() {
    return exchange;
  }

  @Override
  public String routingKey() {
    return routingKey;
  }
}
