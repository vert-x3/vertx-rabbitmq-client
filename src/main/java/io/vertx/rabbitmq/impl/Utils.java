package io.vertx.rabbitmq.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;

import io.vertx.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class Utils {

  public static void populate(JsonObject json, Envelope envelope) {
    if (envelope == null) return;

    put("deliveryTag", envelope.getDeliveryTag(), json);
    put("isRedeliver", envelope.isRedeliver(), json);
    put("exchange", envelope.getExchange(), json);
    put("routingKey", envelope.getRoutingKey(), json);
  }

  public static JsonObject toJson(AMQP.Queue.DeclareOk queueDeclare) {
    if (queueDeclare == null) return null;
    JsonObject json = new JsonObject();
    put("queue", queueDeclare.getQueue(), json);
    put("messageCount", queueDeclare.getMessageCount(), json);
    put("consumerCount", queueDeclare.getConsumerCount(), json);

    return json;
  }

  public static JsonObject toJson(AMQP.Queue.DeleteOk queueDelete) {
    if (queueDelete == null) return null;
    JsonObject json = new JsonObject();
    put("messageCount", queueDelete.getMessageCount(), json);

    return json;
  }

  public static JsonObject toJson( AMQP.BasicProperties properties ) {
    if ( properties == null ) {
      return null;
    }

    JsonObject json = new JsonObject();
    put( "contentType", properties.getContentType(), json );
    put( "contentEncoding", properties.getContentEncoding(), json );
    put( "headers", convertMapLongStringToString( properties.getHeaders() ), json );
    put( "deliveryMode", properties.getDeliveryMode(), json );
    put( "priority", properties.getPriority(), json );
    put( "correlationId", properties.getCorrelationId(), json );
    put( "replyTo", properties.getReplyTo(), json );
    put( "expiration", properties.getExpiration(), json );
    put( "messageId", properties.getMessageId(), json );
    put( "timestamp", properties.getTimestamp(), json );
    put( "type", properties.getType(), json );
    put( "userId", properties.getUserId(), json );
    put( "appId", properties.getAppId(), json );
    put( "clusterId", properties.getClusterId(), json );

    return json;
  }

  /**
   * Converts all values of the given Map and type LongString to String.
   * In case of map is null, null will be directly returned.
   *
   * @return consolidated map.
   */
  private static Map<String, Object> convertMapLongStringToString( Map<String, Object> map ) {

    if (map == null) {
      return map;
    }

    return map.entrySet().stream().collect( Collectors.toMap(
      Map.Entry::getKey, e -> Utils.convertLongStringToString( e.getValue() ) ) );

  }

  /**
   * Converts the given object in case of a LongString or List including a LongString to String.
   *
   * @return consolidated object
   */
  private static Object convertLongStringToString( Object value ) {

    if ( value instanceof LongString ) {
      return value.toString();
    }

    if ( value instanceof List ) {
      List<Object> newList = new ArrayList<>();
      for ( Object item : (List<?>) value ) {
        newList.add( convertLongStringToString( item ) );
      }
      return newList;
    }

    return value;
  }

  public static AMQP.BasicProperties fromJson( JsonObject json ) {
    if ( json == null ) {
      return new AMQP.BasicProperties();
    }

    return new AMQP.BasicProperties.Builder()
      .contentType(json.getString("contentType"))
      .contentEncoding(json.getString("contentEncoding"))
      .headers(asMap(json.getJsonObject("headers")))
      .deliveryMode(json.getInteger("deliveryMode"))
      .priority(json.getInteger("priority"))
      .correlationId(json.getString("correlationId"))
      .replyTo(json.getString("replyTo"))
      .expiration(json.getString("expiration"))
      .messageId(json.getString("messageId"))
      .timestamp(parseDate(json.getString("timestamp")))
      .type(json.getString("type"))
      .userId(json.getString("userId"))
      .appId(json.getString("appId"))
      .clusterId(json.getString("clusterId")).build();
  }

  //TODO: Clean this up, break out into SPI (i.e. BodyCodec)
  public static Object parse(AMQP.BasicProperties properties, byte[] bytes) throws UnsupportedEncodingException {
    if (bytes == null) return null;

    if (properties != null) {
      String encoding = properties.getContentEncoding();
      String ct = properties.getContentType();
      if (ct == null) {
        return decode(encoding, bytes);
      }

      switch (ct) {
        case "application/json":
          return new JsonObject(decode(encoding, bytes));
        case "application/octet-stream":
          return bytes;
        case "text/plain":
        default:
          return decode(encoding, bytes);
      }
    } else {
      return decode(null, bytes);
    }
  }

  public static String decode(String encoding, byte[] bytes) throws UnsupportedEncodingException {
    if (encoding == null) {
      return new String(bytes, "UTF-8");
    } else {
      return new String(bytes, encoding);
    }
  }

  public static byte[] encode(String encoding, String string) throws UnsupportedEncodingException {
    if (encoding == null) {
      return string.getBytes();
    } else {
      return string.getBytes(encoding);
    }
  }

  public static void put(String field, Object value, JsonObject json) {
    if (value != null) {
      json.put(field, value);
    }
  }

  public static void put(String field, Date value, JsonObject json) {
    if (value == null) return;

    OffsetDateTime date = OffsetDateTime.ofInstant(value.toInstant(), ZoneId.of("UTC"));
    String format = date.format(dateTimeFormatter);
    json.put(field, format);
  }

  public static Date parseDate(String date) {
    if (date == null) return null;

    OffsetDateTime odt = OffsetDateTime.parse(date, dateTimeFormatter);
    return Date.from(odt.toInstant());
  }

  public static Map<String, Object> asMap(JsonObject json) {
    if (json == null) return null;

    return json.getMap();
  }

  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
}
