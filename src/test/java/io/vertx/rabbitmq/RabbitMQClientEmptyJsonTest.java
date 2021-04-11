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

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

/**
 * Unit test for Issue #124: Client hangs on when trying to create options from empty config
 * @author jtalbut
 */
public class RabbitMQClientEmptyJsonTest {
  
  @Test
  public void testStart() {
    Vertx vertx = Vertx.vertx();
    JsonObject config = new JsonObject();

    RabbitMQClient.create(vertx, new RabbitMQOptions(config));
  }
  
}
