package io.vertx.rabbitmq.tests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.test.tls.Cert;
import io.vertx.test.tls.Trust;

@RunWith(VertxUnitRunner.class)
public class RabbitMQClientTestBaseTLS {
  protected static final String MOUNT_PATH = "/etc/";
  protected static final String CA_CERT_PATH = "tls/int-ca/ca-cert-root-ca.pem";
  protected static PemKeyCertOptions SERVER = Cert.SERVER_PEM.get();

  protected static final JksOptions TRUSTED = new JksOptions().setPath(Trust.SERVER_JKS.get().getPath());
  protected static final JksOptions UN_TRUSTED = new JksOptions().setPath(Trust.CLIENT_JKS.get().getPath());


  protected RabbitMQClient client;
  protected Vertx vertx;


  protected void connect(RabbitMQOptions config) throws Exception {
    if (client != null) {
      throw new IllegalStateException("Client already started");
    }

    client = RabbitMQClient.create(vertx, config);
    CompletionStage<Void> latch = client.start().toCompletionStage();
    try {
      latch.toCompletableFuture().get(100L, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      rethrow(e.getCause());;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void rethrow(Throwable t) throws T {
    throw (T) t;
  }

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    CompletableFuture<Void> latch = new CompletableFuture<>();
    client.stop().onComplete(ar -> {
      if (ar.succeeded()) {
        latch.complete(null);
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    latch.get(100L, TimeUnit.SECONDS);
    client = null;
    if (vertx != null) {
      vertx.close().onComplete(ctx.asyncAssertSuccess());
      vertx = null;
    }
  }
}
