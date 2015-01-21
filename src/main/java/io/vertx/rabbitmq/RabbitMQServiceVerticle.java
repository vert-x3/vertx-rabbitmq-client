package io.vertx.rabbitmq;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQServiceVerticle extends AbstractVerticle {
  private RabbitMQService service;

  @Override
  public void start() throws Exception {
    // Create the service object
    service = RabbitMQService.create(vertx, config());

    // Register it on the event bus against the configured address
    String address = config().getString("address");
    if (address == null) {
      throw new IllegalStateException("address field must be specified in config for service verticle");
    }
    ProxyHelper.registerService(RabbitMQService.class, vertx, service, address);

    // Start it
    service.start();
  }

  @Override
  public void stop() throws Exception {
    if (service != null) {
      service.stop();
    }
  }
}
