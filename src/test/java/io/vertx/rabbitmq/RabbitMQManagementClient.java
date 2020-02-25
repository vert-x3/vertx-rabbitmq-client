package io.vertx.rabbitmq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class RabbitMQManagementClient {

  private WebClient webClient;
  private final String host;
  private final Integer port;
  private final String user;
  private final String password;

  public RabbitMQManagementClient(Vertx vertx, String host, Integer port, String user, String password) {
    this.webClient = WebClient.create(vertx);
    this.host = host;
    this.port = port;
    this.user = user;
    this.password = password;
  }

  public void getQueueBindings(String queue, String exchange, Handler<AsyncResult<List<Binding>>> resultHandler) {
    String url = "/api/bindings/%2f/e/" + exchange + "/q/" + queue;
    getBindings(url, resultHandler);
  }

  public void getExchangeBindings(String destination, String source, Handler<AsyncResult<List<Binding>>> resultHandler) {
    String url = "/api/bindings/%2f/e/" + source + "/e/" + destination;
    getBindings(url, resultHandler);
  }

  private void getBindings(String url, Handler<AsyncResult<List<Binding>>> resultHandler) {
    this.webClient.get(port, host, url)
      .basicAuthentication(user, password)
      .send(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          if (response.bodyAsString() == null) {
            resultHandler.handle(Future.succeededFuture(Collections.emptyList()));

          } else {
            List<Binding> bindings = asList(response.bodyAsJson(Binding[].class));
            resultHandler.handle(Future.succeededFuture(bindings));
          }

        } else {
          resultHandler.handle(Future.failedFuture(ar.cause()));
        }
      });
  }

  public void getExchange(String name, Handler<AsyncResult<Exchange>> resultHandler) {
    String url = "/api/exchanges/%2f/" + name;
    this.webClient.get(port, host, url)
      .basicAuthentication(user, password)
      .send(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();

          if (response.statusCode() == 404) {
            resultHandler.handle(Future.succeededFuture(null));
          } else {
            resultHandler.handle(Future.succeededFuture(response.bodyAsJson(Exchange.class)));
          }

        } else {
          resultHandler.handle(Future.failedFuture(ar.cause()));
        }
      });
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Binding {
    @JsonProperty("routing_key")
    private String routingKey;
    private Map<String, Object> arguments;

    public String getRoutingKey() {
      return routingKey;
    }

    public void setRoutingKey(String routingKey) {
      this.routingKey = routingKey;
    }

    public Map<String, Object> getArguments() {
      return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
      this.arguments = arguments;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Exchange {
    private String name;
    private Map<String, Object> arguments;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Map<String, Object> getArguments() {
      return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
      this.arguments = arguments;
    }
  }
}
