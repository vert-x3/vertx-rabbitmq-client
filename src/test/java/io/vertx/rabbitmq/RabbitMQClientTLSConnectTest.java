package io.vertx.rabbitmq;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.net.ssl.SSLHandshakeException;

import org.junit.Ignore;
import org.junit.Test;

import io.vertx.core.net.JksOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.test.tls.Trust;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RabbitMQClientTLSConnectTest extends RabbitMQClientTestBaseTLS {
  
  @Test
  public void shouldConnectWithoutHostVerification(TestContext ctx) throws Exception {
	  connect(config()
			  .setTlsEnabled(true)
			  .setHostVerificationEnabled(false));
	  assertTrue(this.client.isConnected());
  }
  
  @Test
  public void shouldConnectWithCustomTrustStore(TestContext ctx) throws Exception {
	  connect(config()
			  .setTlsEnabled(true)
			  .setTlsTrustStore(Trust.SERVER_JKS.get().getPath()));
	  assertTrue(this.client.isConnected());  
  }
  
  @Ignore("test must be run with -Djavax.net.ssl.trustStore=./src/test/resources/tls/client/cacerts.jks")
  @Test
  public void shouldConnectWithDefaultTrustStore(TestContext ctx) throws Exception {
	  connect(config()
			  .setTlsEnabled(true)
	  );
	  assertTrue(this.client.isConnected());  
  }
  @Test
  public void shouldRejectUntrustedServer(TestContext ctx)  {
	  try {
		connect(config()
				  .setTlsEnabled(true)
				  .setTlsTrustStorePassword("wibble")
				  .setTlsTrustStore(Trust.CLIENT_JKS.get().getPath())
		  );
	} catch (Exception e) {
		assertFalse(client.isConnected());  
		assertTrue(e.getCause() instanceof SSLHandshakeException);
	}
	   
  }
}