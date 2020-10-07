package io.vertx.rabbitmq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.net.ssl.SSLHandshakeException;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.lang.exception.ExceptionUtils;

import io.vertx.ext.unit.TestContext;
import io.vertx.test.tls.Trust;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQClientTLSConnectTest extends RabbitMQClientTestBaseTLS {
	@ClassRule
	public static final GenericContainer rabbitmq = new GenericContainer("rabbitmq:3.7").withExposedPorts(5671)
			.withClasspathResourceMapping(CA_CERT_PATH, MOUNT_PATH + CA_CERT_PATH, BindMode.READ_ONLY)
			.withClasspathResourceMapping(SERVER.getCertPath(), MOUNT_PATH + SERVER.getCertPath(), BindMode.READ_ONLY)
			.withClasspathResourceMapping(SERVER.getKeyPath(), MOUNT_PATH + SERVER.getKeyPath(), BindMode.READ_ONLY)

			.withEnv("RABBITMQ_SSL_CACERTFILE", MOUNT_PATH + CA_CERT_PATH)
			.withEnv("RABBITMQ_SSL_CERTFILE", MOUNT_PATH + SERVER.getCertPath())
			.withEnv("RABBITMQ_SSL_KEYFILE", MOUNT_PATH + SERVER.getKeyPath()).withEnv("RABBITMQ_SSL_VERIFY", "verify_peer")
			.withEnv("RABBITMQ_SSL_FAIL_IF_NO_PEER_CERT", "false")
			.waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1));

	private RabbitMQOptions config() throws Exception {
		RabbitMQOptions config = new RabbitMQOptions();
		config.setUri("amqp://" + rabbitmq.getContainerIpAddress() + ":" + rabbitmq.getMappedPort(5671));
		config.setPort(rabbitmq.getMappedPort(5671));
		return config;
	}
	
	@Test
	public void shouldPropagateCausingExeption(TestContext ctx) throws Throwable {
		try {
		  connect(new RabbitMQOptions()
				.setUri("amqp://" + rabbitmq.getContainerIpAddress() + ": A32")
				);
		}catch(Exception e) {
			assertTrue(ExceptionUtils.getRootCause(e) instanceof URISyntaxException);
		}		
	}

	@Test
	public void shouldConnectWithoutHostVerification(TestContext ctx) throws Exception {
		connect(config()
				.setSsl(true)
				.setTrustAll(true));
		assertTrue(this.client.isConnected());
	}
	
	@Test
	public void shouldFailConnectingWithPlainText(TestContext ctx) {
		try {
			connect(config());
			fail("Should have thrown exception");
		} catch (Exception e) {
			assertFalse(client.isConnected());
			assertTrue(e.getCause() instanceof IOException);
		}
	}

	@Test
	public void shouldConnectWithCustomTrustStore(TestContext ctx) throws Exception {
		connect(config()
				.setSsl(true)
				.setTrustOptions(TRUSTED));
		assertTrue(this.client.isConnected());
	}
	
	@Test
	public void shouldConnectWithPemTrustStore(TestContext ctx) throws Exception {
		connect(config()
				.setSsl(true)
				.setPemTrustOptions(Trust.SERVER_PEM.get()));
		assertTrue(this.client.isConnected());
	}

	@Test
	public void shouldRejectUntrustedServer(TestContext ctx) {
		try {
			connect(config()
					.setSsl(true)
					.setTrustOptions(UN_TRUSTED));
			fail("Should have thrown exception");
		} catch (Exception e) {
			assertFalse(client.isConnected());
			assertTrue(e.getCause() instanceof SSLHandshakeException);
		}
	}
}