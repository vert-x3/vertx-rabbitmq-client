package io.vertx.rabbitmq;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQServiceVerticle extends AbstractVerticle {
  private RabbitMQService service;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    // Create the service object
    service = RabbitMQService.create(vertx, config());

    // Register it on the event bus against the configured address
    String address = config().getString("address");
    if (address == null) {
      throw new IllegalStateException("address field must be specified in config for service verticle");
    }
    ProxyHelper.registerService(RabbitMQService.class, vertx, service, address);

    // Start it
    service.start(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    if (service != null) {
      service.stop(ar -> {
        if (ar.succeeded()) {
          stopFuture.complete();
        } else {
          stopFuture.fail(ar.cause());
        }
      });
    }
  }
}
