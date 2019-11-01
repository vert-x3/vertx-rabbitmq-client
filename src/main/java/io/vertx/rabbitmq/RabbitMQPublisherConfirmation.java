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

/**
 *
 * @author njt
 */
@DataObject(generateConverter = true)
public class RabbitMQPublisherConfirmation {

  private final String messageId;
  private final boolean succeeded;

  public RabbitMQPublisherConfirmation(String messageId, boolean succeeded) {
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
