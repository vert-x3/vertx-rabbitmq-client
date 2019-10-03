package io.vertx.rabbitmq.impl;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.*;

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

    Map<String, Object> customListMap = new HashMap<>();
    customListMap.put( "count", 4L);
    customListMap.put( "exchange", LongStringHelper.asLongString( "exchange1" ) );
    customListMap.put( "reason", LongStringHelper.asLongString( "expired" ) );
    customListMap.put( "routing-keys", Arrays.asList(
      LongStringHelper.asLongString( "key1" ),
      LongStringHelper.asLongString( "key2" )
    ));
    customListMap.put( "time", Date.from( Instant.ofEpochSecond( 1570140666 ) ) );

    List<Map<String, Object>> customList = new ArrayList<>();
    customList.add(customListMap);

    Map<String, Object> headers = new HashMap<>();
    headers.put( "customId", 123 );
    headers.put( "customQualifier", "qualifier1" );
    headers.put( "customQualifierLongString", LongStringHelper.asLongString( "qualifier2" ) );
    headers.put( "customList", customList );

    JsonObject result = Utils.toJson( new BasicPropertiesTestBuilder().setHeaders( headers ).build() );
    Map<String, Object> headersConverted = result.getJsonObject( "headers" ).getMap();
    assertNotNull( headersConverted );
    assertEquals( 4, headersConverted.entrySet().size() );
    assertEquals( 123, headersConverted.get( "customId" ) );
    assertEquals( "qualifier1", headersConverted.get( "customQualifier" ) );
    assertEquals( "qualifier2", headersConverted.get( "customQualifierLongString" ) );
    assertNotNull( headersConverted.get( "customList" ) );

    List<Map<String, Object>> headersConvertedCustomList = (List<Map<String, Object>>) headersConverted.get( "customList");
    assertEquals( 1, headersConvertedCustomList.size() );
    assertEquals( 4L, headersConvertedCustomList.get(0).get("count") );
    assertEquals( "exchange1", headersConvertedCustomList.get(0).get("exchange") );
    assertEquals( "expired", headersConvertedCustomList.get(0).get("reason") );
    assertEquals( Instant.parse("2019-10-03T22:11:06Z"), headersConvertedCustomList.get(0).get("time") );

    List<String> headersConvertedCustomListMapRoutingKeys = (List<String>) headersConvertedCustomList.get(0).get("routing-keys");

    assertNotNull( headersConvertedCustomListMapRoutingKeys );
    assertEquals( 2, headersConvertedCustomListMapRoutingKeys.size() );
    assertEquals( "key1", headersConvertedCustomListMapRoutingKeys.get(0) );
    assertEquals( "key2", headersConvertedCustomListMapRoutingKeys.get(1) );
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
