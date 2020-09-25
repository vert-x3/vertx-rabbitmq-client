package io.vertx.rabbitmq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.vertx.core.Vertx;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.test.tls.Cert;

@RunWith(VertxUnitRunner.class)
public class RabbitMQClientTestBaseTLS {
  private static final String MOUNT_PATH = "/etc/";
  private static PemKeyCertOptions CA = Cert.SERVER_PEM_ROOT_CA.get();
  private static PemKeyCertOptions SERVER = Cert.SERVER_PEM.get();

  protected RabbitMQClient client;
  protected Vertx vertx;
  
  
  
  @ClassRule
  public static final GenericContainer rabbitmq = new GenericContainer("rabbitmq:3.7")
	.withExposedPorts(5671)  
    .withClasspathResourceMapping(CA.getCertPath(), MOUNT_PATH+CA.getCertPath(), BindMode.READ_ONLY)
    .withClasspathResourceMapping(SERVER.getCertPath(),  MOUNT_PATH+SERVER.getCertPath(), BindMode.READ_ONLY)
    .withClasspathResourceMapping(SERVER.getKeyPath(),   MOUNT_PATH+SERVER.getKeyPath(), BindMode.READ_ONLY)
    
    .withEnv("RABBITMQ_SSL_CACERTFILE", MOUNT_PATH+CA.getCertPath())
    .withEnv("RABBITMQ_SSL_CERTFILE",  MOUNT_PATH+SERVER.getCertPath())
    .withEnv("RABBITMQ_SSL_KEYFILE",  MOUNT_PATH+SERVER.getKeyPath())
    .withEnv("RABBITMQ_SSL_VERIFY",  "verify_peer")
    .withEnv("RABBITMQ_SSL_FAIL_IF_NO_PEER_CERT",  "false")
    .waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1))
    ;
  
 
  protected void connect(RabbitMQOptions config) throws Exception {
    if (client != null) {
      throw new IllegalStateException("Client already started");
    }
    
    client = RabbitMQClient.create(vertx, config);
    CompletableFuture<Void> latch = new CompletableFuture<>();
    client.start(ar -> {
      if (ar.succeeded()) {
        latch.complete(null);
      } else {
        latch.completeExceptionally(ar.cause());
      }
    });
    latch.get(100L, TimeUnit.SECONDS);
  }

  public RabbitMQOptions config() throws Exception {
    RabbitMQOptions config = new RabbitMQOptions();
    config.setUri("amqp://" + rabbitmq.getContainerIpAddress() + ":" + rabbitmq.getMappedPort(5671));
    config.setPort(rabbitmq.getMappedPort(5671));
    return config;
  }

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
    
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
	CompletableFuture<Void> latch = new CompletableFuture<>();
	client.stop(ar -> {
          if (ar.succeeded()) {
            latch.complete(null);
          } else {
            latch.completeExceptionally(ar.cause());
          }
        });
    latch.get(100L, TimeUnit.SECONDS);
    if (vertx != null) {
      vertx.close(ctx.asyncAssertSuccess());    
    }
  }

}
