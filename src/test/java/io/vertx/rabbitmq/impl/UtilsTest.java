package io.vertx.rabbitmq.impl;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.impl.LongStringHelper;

import io.vertx.core.json.JsonObject;

public class UtilsTest {

  @Test
  public void testConvertWithHeadersNull() {
    JsonObject result = Utils.toJson( new BasicPropertiesTestBuilder().build() );
    assertEquals( "application/json", result.getString( "contentType" ) );
    assertEquals( "app1", result.getString( "appId" ) );
    assertEquals( null, result.getString( "headers" ) );
  }

  @Test
  public void testConvertWithHeadersEmpty() {
    JsonObject result = Utils.toJson( new BasicPropertiesTestBuilder().setHeaders( Collections.emptyMap() ).build() );

    JsonObject headersConverted = result.getJsonObject( "headers" );
    assertNotNull( headersConverted );
    assertTrue( headersConverted.isEmpty() );
  }


  @Test
  public void testConvertWithHeaders() {

    Map<String, Object> headers = new HashMap<>();
    headers.put( "customId", 123 );
    headers.put( "customQualifier", "qualifier1" );
    headers.put( "customQualifierLongString", LongStringHelper.asLongString( "qualifier2" ) );

    JsonObject result = Utils.toJson( new BasicPropertiesTestBuilder().setHeaders( headers ).build() );
    Map<String, Object> headersConverted = result.getJsonObject( "headers" ).getMap();
    assertNotNull( headersConverted );
    assertEquals( 3, headersConverted.entrySet().size() );
    assertEquals( 123, headersConverted.get( "customId" ) );
    assertEquals( "qualifier1", headersConverted.get( "customQualifier" ) );
    assertEquals( "qualifier2", headersConverted.get( "customQualifierLongString" ) );
  }

  /**
   * Private fluent builder to create {@code BasicProperties}.
   */
  private class BasicPropertiesTestBuilder {

    private String contentType;
    private String contentEncoding;
    private Map<String, Object> headers;
    private int deliveryMode;
    private int priority;
    private String correlationId;
    private String replyTo;
    private String expiration;
    private String messageId;
    private String userId;
    private Date timestamp;
    private String type;
    private String appId;
    private String clusterId;

    private BasicPropertiesTestBuilder() {
      contentType = "application/json";
      contentEncoding = "UTF-8";
      deliveryMode = 1;
      priority = 1;
      correlationId = "test";
      messageId = "msg1";
      timestamp = new Date();
      appId = "app1";
      userId = "user1";
    }

    public BasicPropertiesTestBuilder setContentType( String contentType ) {
      this.contentType = contentType;
      return this;
    }

    public BasicPropertiesTestBuilder setContentEncoding( String contentEncoding ) {
      this.contentEncoding = contentEncoding;
      return this;
    }

    public BasicPropertiesTestBuilder setHeaders( Map<String, Object> headers ) {
      this.headers = headers;
      return this;
    }

    public BasicPropertiesTestBuilder setDeliveryMode( int deliveryMode ) {
      this.deliveryMode = deliveryMode;
      return this;
    }

    public BasicPropertiesTestBuilder setPriority( int priority ) {
      this.priority = priority;
      return this;
    }

    public BasicPropertiesTestBuilder setCorrelationId( String correlationId ) {
      this.correlationId = correlationId;
      return this;
    }

    public BasicPropertiesTestBuilder setReplyTo( String replyTo ) {
      this.replyTo = replyTo;
      return this;
    }

    public BasicPropertiesTestBuilder setExpiration( String expiration ) {
      this.expiration = expiration;
      return this;
    }

    public BasicPropertiesTestBuilder setMessageId( String messageId ) {
      this.messageId = messageId;
      return this;
    }

    public BasicPropertiesTestBuilder setUserId( String userId ) {
      this.userId = userId;
      return this;
    }

    public BasicPropertiesTestBuilder setTimestamp( Date timestamp ) {
      this.timestamp = timestamp;
      return this;
    }

    public BasicPropertiesTestBuilder setType( String type ) {
      this.type = type;
      return this;
    }

    public BasicPropertiesTestBuilder setAppId( String appId ) {
      this.appId = appId;
      return this;
    }

    public BasicPropertiesTestBuilder setClusterId( String clusterId ) {
      this.clusterId = clusterId;
      return this;
    }

    private AMQP.BasicProperties build() {
      return new AMQP.BasicProperties( contentType, contentEncoding, headers, deliveryMode, priority,
        correlationId, replyTo, expiration, messageId, timestamp, type, userId, appId, clusterId );
    }

  }

}
