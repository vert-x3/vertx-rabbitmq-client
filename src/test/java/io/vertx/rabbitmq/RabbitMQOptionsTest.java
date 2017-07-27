package io.vertx.rabbitmq;

import io.vertx.core.json.JsonObject;
import io.vertx.test.core.TestUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class RabbitMQOptionsTest {

  @Test
  public void testFromJsonAndCopy() {
    String expectedUri = TestUtils.randomAlphaString(50);
    String expectedUser = TestUtils.randomAlphaString(20);
    String expectedPassword = TestUtils.randomAlphaString(20);
    String expectedHost = TestUtils.randomAlphaString(20);
    int expectedPort = TestUtils.randomPortInt();
    String expectedVirtualHost = "/" + TestUtils.randomAlphaString(10);
    int expectedConnectionTimeout = TestUtils.randomPositiveInt();
    int expectedRequestedHeartbeat = TestUtils.randomPositiveInt();
    int expectedHandshakeTimeout = TestUtils.randomPositiveInt();
    int expectedRequestedChannelMax = TestUtils.randomPositiveInt();
    int expectedNetworkRecoveryInterval = TestUtils.randomPositiveInt();
    boolean expectedAutomaticRecoveryEnabled = TestUtils.randomBoolean();
    int expectedConnectionRetryDelay = TestUtils.randomPositiveInt();
    Integer expectedConnectionRetries = TestUtils.randomBoolean() ? TestUtils.randomPositiveInt() : null;
    boolean expectedIncludeProperties = TestUtils.randomBoolean();
    JsonObject json = new JsonObject();
    json.put("uri", expectedUri);
    json.put("user", expectedUser);
    json.put("password", expectedPassword);
    json.put("host", expectedHost);
    json.put("port", expectedPort);
    json.put("virtualHost", expectedVirtualHost);
    json.put("connectionTimeout", expectedConnectionTimeout);
    json.put("requestedHeartbeat", expectedRequestedHeartbeat);
    json.put("handshakeTimeout", expectedHandshakeTimeout);
    json.put("requestedChannelMax", expectedRequestedChannelMax);
    json.put("networkRecoveryInterval", expectedNetworkRecoveryInterval);
    json.put("automaticRecoveryEnabled", expectedAutomaticRecoveryEnabled);
    json.put("connectionRetryDelay", expectedConnectionRetryDelay);
    json.put("connectionRetries", expectedConnectionRetries);
    json.put("includeProperties", expectedIncludeProperties);
    RabbitMQOptions options = new RabbitMQOptions();
    assertSame(options, options.setUri(expectedUri));
    assertSame(options, options.setUser(expectedUser));
    assertSame(options, options.setPassword(expectedPassword));
    assertSame(options, options.setHost(expectedHost));
    assertSame(options, options.setPort(expectedPort));
    assertSame(options, options.setVirtualHost(expectedVirtualHost));
    assertSame(options, options.setConnectionTimeout(expectedConnectionTimeout));
    assertSame(options, options.setRequestedHeartbeat(expectedRequestedHeartbeat));
    assertSame(options, options.setHandshakeTimeout(expectedHandshakeTimeout));
    assertSame(options, options.setRequestedChannelMax(expectedRequestedChannelMax));
    assertSame(options, options.setNetworkRecoveryInterval(expectedNetworkRecoveryInterval));
    assertSame(options, options.setAutomaticRecoveryEnabled(expectedAutomaticRecoveryEnabled));
    assertSame(options, options.setConnectionRetryDelay(expectedConnectionRetryDelay));
    assertSame(options, options.setConnectionRetries(expectedConnectionRetries));
    assertSame(options, options.setIncludeProperties(expectedIncludeProperties));
    for (RabbitMQOptions testOptions : Arrays.asList(new RabbitMQOptions(json), new RabbitMQOptions(options))) {
      assertEquals(testOptions.getUri(), expectedUri);
      assertEquals(testOptions.getUser(), expectedUser);
      assertEquals(testOptions.getPassword(), expectedPassword);
      assertEquals(testOptions.getHost(), expectedHost);
      assertEquals(testOptions.getPort(), expectedPort);
      assertEquals(testOptions.getVirtualHost(), expectedVirtualHost);
      assertEquals(testOptions.getConnectionTimeout(), expectedConnectionTimeout);
      assertEquals(testOptions.getRequestedHeartbeat(), expectedRequestedHeartbeat);
      assertEquals(testOptions.getHandshakeTimeout(), expectedHandshakeTimeout);
      assertEquals(testOptions.getRequestedChannelMax(), expectedRequestedChannelMax);
      assertEquals(testOptions.getNetworkRecoveryInterval(), expectedNetworkRecoveryInterval);
      assertEquals(testOptions.isAutomaticRecoveryEnabled(), expectedAutomaticRecoveryEnabled);
      assertEquals(testOptions.getConnectionRetryDelay(), expectedConnectionRetryDelay);
      assertEquals(testOptions.getConnectionRetries(), expectedConnectionRetries);
      assertEquals(testOptions.getIncludeProperties(), expectedIncludeProperties);
    }
  }
}
