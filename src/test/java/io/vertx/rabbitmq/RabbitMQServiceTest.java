package io.vertx.rabbitmq;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQServiceTest extends RabbitMQServiceTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = RabbitMQService.create(vertx, config());
    service.start();
  }
}
