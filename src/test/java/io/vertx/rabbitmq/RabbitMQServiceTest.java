package io.vertx.rabbitmq;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQServiceTest extends RabbitMQServiceTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = RabbitMQService.create(vertx, config());
    CountDownLatch latch = new CountDownLatch(1);
    service.start(onSuccess(v -> {
      latch.countDown();
    }));
    awaitLatch(latch);
  }
}
