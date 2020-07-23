/*
 * Copyright 2019 Eclipse.
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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author njt
 */
@DataObject(generateConverter = true)
public class RabbitMQPublisherConfirmation {

  private String messageId;
  private boolean succeeded;

  public RabbitMQPublisherConfirmation(JsonObject json) {
    RabbitMQPublisherConfirmationConverter.fromJson(json, this);
  }
  
  public RabbitMQPublisherConfirmation(String messageId, boolean succeeded) {
    this.messageId = messageId;
    this.succeeded = succeeded;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RabbitMQPublisherConfirmationConverter.toJson(this, json);
    return json;
  }
  
  public String getMessageId() {
    return messageId;
  }

  public boolean isSucceeded() {
    return succeeded;
  }

}
