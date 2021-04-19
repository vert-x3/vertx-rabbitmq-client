/*
 * Copyright 2021 Eclipse.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.rabbitmq;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for Issue #124: Client hangs on when trying to create options from empty config
 *
 * @author jtalbut
 */
@RunWith(VertxUnitRunner.class)
public class RabbitMQClientEmptyJsonTest {

  private static final Logger LOG = LoggerFactory.getLogger(RabbitMQClientEmptyJsonTest.class);

  public final class NotificationsVerticle extends AbstractVerticle {

    private static final String QUEUE_NAME = "subscriber-available";

    private static final String NOTIFICATIONS_ADDRESS = "notifications";

    private final TestContext testContext;
    private final Async async;

    public NotificationsVerticle(TestContext testContext, Async async) {
      this.testContext = testContext;
      this.async = async;
    }
    
    @Override
    public void start() {

      connectToRabbit()
              .compose(this::listenForNotifications)
              .onSuccess(v -> {
                LOG.info("Notifications service is ready.");
                testContext.fail("Rabbit connected, despite not having host, url or address");
              })
              .onFailure(error -> {
                LOG.error(error.getMessage(), error);
                async.complete();
              });
    }

    private Future<RabbitMQClient> connectToRabbit() {

      System.out.println(config().getJsonObject("rabbitmq"));

      try {
        final RabbitMQClient rabbit = RabbitMQClient.create(
                vertx,
                new RabbitMQOptions(config())
                        .setAutomaticRecoveryEnabled(false)
                        .setReconnectAttempts(Integer.MAX_VALUE)
                        .setAutomaticRecoveryOnInitialConnection(false)
                        .setReconnectInterval(500));
        return rabbit
                .start()
                .map(rabbit)
                ;

      } catch(Throwable ex) {
        LOG.error("Caught: ", ex);
        testContext.fail("Caught exception");
        return Future.failedFuture(ex);
      }
    }

    private Future<Void> listenForNotifications(final RabbitMQClient rabbit) {

      final Integer period = config().getInteger("queue_process_period");

      vertx.setPeriodic(period, timer -> {
        rabbit.basicGet(QUEUE_NAME, true, get -> {
          if (get.succeeded()) {
            processMessage(get.result());
          } else {
            LOG.warn("Can not read message from queue.");
          }
        });
      });

      return Future.succeededFuture();
    }

    private void processMessage(final RabbitMQMessage message) {

      final JsonObject body = message.body().toJsonObject();

      LOG.info("New notification: {}.", body.encode());

      vertx.eventBus().publish(NOTIFICATIONS_ADDRESS, body);
    }
  }

  @Test
  public void testStart(TestContext testContext) throws InterruptedException {
    Vertx vertx = Vertx.vertx();
    
    Async async = testContext.async();
    vertx.deployVerticle(new NotificationsVerticle(testContext, async));
    
    async.awaitSuccess(1000000);
  }

}
