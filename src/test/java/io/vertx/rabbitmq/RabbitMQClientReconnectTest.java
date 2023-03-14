package io.vertx.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import io.vertx.core.net.*;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RabbitMQClientReconnectTest extends RabbitMQClientTestBase {

  private static final int PROXY_PORT = 8000;

  protected Integer connectionRetries = 0;
  protected long connectionRetryDelay = RabbitMQOptions.DEFAULT_RECONNECT_INTERVAL;
  private NetServer proxyServer;
  private NetClient proxyClient;

  private void startProxy(int numDisconnects) throws Exception {
    CompletableFuture<Void> latch = new CompletableFuture<>();
    RabbitMQOptions config = super.config();
    ConnectionFactory cf = new ConnectionFactory();
    NetClientOptions clientOptions = new NetClientOptions();
    if (config.getUri() != null) {
      cf.setUri(config.getUri());
      if (cf.isSSL()) {
        clientOptions.setSsl(true);
        clientOptions.setTrustAll(true);
      }
    } else {
      cf.setPort(config.getPort());
      cf.setHost(config.getHost());
    }
    String host = cf.getHost();
    int port = cf.getPort();
    proxyClient = vertx.createNetClient(clientOptions);
    AtomicInteger remaining = new AtomicInteger(numDisconnects);
    proxyServer = vertx.createNetServer().connectHandler(serverSocket -> {
      if (remaining.getAndDecrement() > 0) {
        serverSocket.close();
      } else {
        serverSocket.pause();
        proxyClient.connect(port, host).onComplete(ar -> {
          if (ar.succeeded()) {
            NetSocket clientSocket = ar.result();
            serverSocket.handler(clientSocket::write);
            serverSocket.exceptionHandler(err -> serverSocket.close());
            serverSocket.closeHandler(v -> clientSocket.close());
            clientSocket.handler(serverSocket::write);
            clientSocket.exceptionHandler(err -> clientSocket.close());
            clientSocket.closeHandler(v -> serverSocket.close());
            serverSocket.resume();
          } else {
            serverSocket.close();;
          }
        });
      }
    });
    proxyServer.listen(PROXY_PORT, "localhost").onComplete(ar -> {
      if (ar.succeeded()) {
        latch.complete(null);
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    latch.get(10, TimeUnit.SECONDS);
  }

  @Override
  public void tearDown(TestContext ctx) throws Exception {
    super.tearDown(ctx);
    if (proxyServer != null) {
      proxyServer.close();
    }
    if (proxyClient != null) {
      proxyClient.close();
    }
  }

  @Override
  public RabbitMQOptions config() throws Exception {
    RabbitMQOptions cfg = super.config();
    String username;
    String password;
    if (cfg.getUri() != null) {
      ConnectionFactory cf = new ConnectionFactory();
      cf.setUri(cfg.getUri());
      username = cf.getUsername();
      password = cf.getPassword();
    } else {
      username = "guest";
      password = "guest";
    }
    String uri = "amqp://" + username +  ":" + password + "@localhost:" + PROXY_PORT;
    return new RabbitMQOptions()
      .setUri(uri)
      .setReconnectAttempts(connectionRetries)
      .setReconnectInterval(connectionRetryDelay);
  }

  @Test
  public void testReconnect(TestContext ctx) throws Exception {
    connectionRetryDelay = 100;
    connectionRetries = 2;
    startProxy(2);
    connect();
    client.stop().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testReconnectFail(TestContext ctx) throws Exception {
    connectionRetryDelay = 100;
    connectionRetries = 2;
    startProxy(3);
    try {
      connect();
      ctx.fail();
    } catch (Exception ignore) {
      // Expected
    }
  }
}
