package io.vertx.rabbitmq;

import io.vertx.core.DeploymentOptions;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQServiceVerticleTest extends RabbitMQServiceTestBase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DeploymentOptions options = new DeploymentOptions().setConfig(config());
    CountDownLatch latch = new CountDownLatch(1);
    vertx.deployVerticle("service:io.vertx.rabbitmq-service", options, onSuccess(id -> {
      service = RabbitMQService.createEventBusProxy(vertx, "vertx.rabbitmq");
      latch.countDown();
    }));
    awaitLatch(latch);
  }
}
