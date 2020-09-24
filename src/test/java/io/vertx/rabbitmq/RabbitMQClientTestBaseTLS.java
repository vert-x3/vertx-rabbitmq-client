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
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class RabbitMQClientTestBaseTLS {
  private static final String RESOURCE_PATH = "./tls/server/";
  private static final String KEY = RESOURCE_PATH+"localhost.key";
  private static final String CERT = RESOURCE_PATH+"localhost.pem";
  private static final String CA_CERT = RESOURCE_PATH+"rootCA.pem";
  private static final String MOUNT_PATH = "/etc/";

  protected RabbitMQClient client;
  protected Vertx vertx;
   
  
  @ClassRule
  public static final GenericContainer rabbitmq = new GenericContainer("rabbitmq:3.7")
	.withExposedPorts(5671)  
    .withClasspathResourceMapping(CA_CERT, MOUNT_PATH+CA_CERT, BindMode.READ_ONLY)
    .withClasspathResourceMapping(CERT,  MOUNT_PATH+CERT, BindMode.READ_ONLY)
    .withClasspathResourceMapping(KEY,  MOUNT_PATH+KEY, BindMode.READ_ONLY)
    .withEnv("RABBITMQ_SSL_CACERTFILE", MOUNT_PATH+CA_CERT)
    .withEnv("RABBITMQ_SSL_CERTFILE",  MOUNT_PATH+CERT)
    .withEnv("RABBITMQ_SSL_KEYFILE",  MOUNT_PATH+KEY)
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
