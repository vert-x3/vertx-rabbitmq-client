package io.vertx.rabbitmq;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.net.ssl.SSLHandshakeException;

import org.junit.Ignore;
import org.junit.Test;

import io.vertx.ext.unit.TestContext;

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
	 
	  ClassLoader classloader = getClass().getClassLoader();
	  String path = classloader.getResource("tls/client/cacerts.jks").getPath();
	  connect(config()
			  .setTlsEnabled(true)
			  .setTlsTrustStore(path)
	  );
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
	  ClassLoader classloader = getClass().getClassLoader();
	  String path = classloader.getResource("tls/client/faultyCAcerts.jks").getPath();
	  try {
		connect(config()
				  .setTlsEnabled(true)
				  .setTlsTrustStorePassword("unittest")
				  .setTlsTrustStore(path)
		  );
	} catch (Exception e) {
		assertFalse(client.isConnected());  
		assertTrue(e.getCause() instanceof SSLHandshakeException);
	}
	   
  }
}