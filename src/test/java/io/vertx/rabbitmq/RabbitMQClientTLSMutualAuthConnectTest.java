package io.vertx.rabbitmq;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.vertx.core.net.JksOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.test.tls.Cert;
import javax.net.ssl.SSLException;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQClientTLSMutualAuthConnectTest extends RabbitMQClientTestBaseTLS {

  @ClassRule
  public static final GenericContainer rabbitmq = new GenericContainer("rabbitmq:3.7").withExposedPorts(5671)
    .withClasspathResourceMapping(CA_CERT_PATH, MOUNT_PATH + CA_CERT_PATH, BindMode.READ_ONLY)
    .withClasspathResourceMapping(SERVER.getCertPath(), MOUNT_PATH + SERVER.getCertPath(), BindMode.READ_ONLY)
    .withClasspathResourceMapping(SERVER.getKeyPath(), MOUNT_PATH + SERVER.getKeyPath(), BindMode.READ_ONLY)

    .withEnv("RABBITMQ_SSL_CACERTFILE", MOUNT_PATH + CA_CERT_PATH)
    .withEnv("RABBITMQ_SSL_CERTFILE", MOUNT_PATH + SERVER.getCertPath())
    .withEnv("RABBITMQ_SSL_KEYFILE", MOUNT_PATH + SERVER.getKeyPath()).withEnv("RABBITMQ_SSL_VERIFY", "verify_peer")
    .withEnv("RABBITMQ_SSL_FAIL_IF_NO_PEER_CERT", "true").withEnv("RABBITMQ_SSL_DEPTH", "4")
    .waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1));

  private RabbitMQOptions config() throws Exception {
    RabbitMQOptions config = new RabbitMQOptions();

    config.setUri("amqp://" + rabbitmq.getContainerIpAddress() + ":" + rabbitmq.getMappedPort(5671));
    return config;
  }

  @Ignore
  @Test
  public void shouldConnectWithMutualAuth(TestContext ctx) throws Exception {


    JksOptions me = new JksOptions()
      .setPassword(Cert.CLIENT_JKS.get().getPassword())
      .setPath("tls/client-keystore-root-ca.jks");

    connect(config()
      .setSsl(true)
      .setTrustOptions(TRUSTED)
      .setKeyCertOptions(me));

    assertTrue(this.client.isConnected());
  }

  @Test
  public void shouldRejectUntrustedClient(TestContext ctx) {
    JksOptions untrusted = new JksOptions()
      .setPassword(Cert.CLIENT_JKS.get().getPassword())
      .setPath(Cert.CLIENT_JKS.get().getPath());
    try {
      connect(config()
        .setSsl(true)
        .setTrustOptions(TRUSTED)
        .setKeyCertOptions(untrusted));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertFalse(client.isConnected());
      assertTrue("Was expecting " + e.getCause() + " to be an instance of SSLException", e.getCause() instanceof SSLException);
    }
  }
}
