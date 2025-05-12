module io.vertx.rabbitmq.client {
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;
  requires static io.vertx.docgen;
  requires com.rabbitmq.client;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires io.netty.handler;
  exports io.vertx.rabbitmq;
}
