package io.vertx.ceylon.rabbitmq;

import com.redhat.ceylon.compiler.java.metadata.Ceylon;
import com.redhat.ceylon.compiler.java.metadata.TypeInfo;
import com.redhat.ceylon.compiler.java.metadata.TypeParameter;
import com.redhat.ceylon.compiler.java.metadata.TypeParameters;
import com.redhat.ceylon.compiler.java.metadata.Variance;
import com.redhat.ceylon.compiler.java.metadata.Ignore;
import com.redhat.ceylon.compiler.java.metadata.Name;
import com.redhat.ceylon.compiler.java.runtime.model.TypeDescriptor;
import com.redhat.ceylon.compiler.java.runtime.model.ReifiedType;
import ceylon.language.Callable;
import ceylon.language.DocAnnotation$annotation$;
import java.util.Map;
import io.vertx.ceylon.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@Ceylon(major = 8)
@DocAnnotation$annotation$(description = "")
public class RabbitMQClient implements ReifiedType {

  @Ignore
  public static final io.vertx.lang.ceylon.ConverterFactory<io.vertx.rabbitmq.RabbitMQClient, RabbitMQClient> TO_CEYLON = new io.vertx.lang.ceylon.ConverterFactory<io.vertx.rabbitmq.RabbitMQClient, RabbitMQClient>() {
    public io.vertx.lang.ceylon.Converter<io.vertx.rabbitmq.RabbitMQClient, RabbitMQClient> converter(final TypeDescriptor... descriptors) {
      return new io.vertx.lang.ceylon.Converter<io.vertx.rabbitmq.RabbitMQClient, RabbitMQClient>() {
        public RabbitMQClient convert(io.vertx.rabbitmq.RabbitMQClient src) {
          return new RabbitMQClient(src);
        }
      };
    }
  };

  @Ignore
  public static final io.vertx.lang.ceylon.Converter<RabbitMQClient, io.vertx.rabbitmq.RabbitMQClient> TO_JAVA = new io.vertx.lang.ceylon.Converter<RabbitMQClient, io.vertx.rabbitmq.RabbitMQClient>() {
    public io.vertx.rabbitmq.RabbitMQClient convert(RabbitMQClient src) {
      return src.delegate;
    }
  };

  @Ignore public static final TypeDescriptor $TypeDescriptor$ = new io.vertx.lang.ceylon.VertxTypeDescriptor(TypeDescriptor.klass(RabbitMQClient.class), io.vertx.rabbitmq.RabbitMQClient.class, TO_JAVA, TO_CEYLON);
  @Ignore private final io.vertx.rabbitmq.RabbitMQClient delegate;

  public RabbitMQClient(io.vertx.rabbitmq.RabbitMQClient delegate) {
    this.delegate = delegate;
  }

  @Ignore 
  public TypeDescriptor $getType$() {
    return $TypeDescriptor$;
  }

  @Ignore
  public Object getDelegate() {
    return delegate;
  }

  @DocAnnotation$annotation$(description = " Acknowledge one or several received messages. Supply the deliveryTag from the AMQP.Basic.GetOk or AMQP.Basic.Deliver\n method containing the received message being acknowledged.\n")
  @TypeInfo("ceylon.language::Anything")
  public void basicAck(
    final @TypeInfo("ceylon.language::Integer") @Name("deliveryTag") long deliveryTag, 
    final @TypeInfo("ceylon.language::Boolean") @Name("multiple") boolean multiple, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable|ceylon.json::Object)") @Name("resultHandler") Callable<?> resultHandler) {
    long arg_0 = deliveryTag;
    boolean arg_1 = multiple;
    io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>> arg_2 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<io.vertx.core.json.JsonObject>(resultHandler) {
      public Object toCeylon(io.vertx.core.json.JsonObject event) {
        return io.vertx.lang.ceylon.ToCeylon.JsonObject.safeConvert(event);
      }
    };
    delegate.basicAck(arg_0, arg_1, arg_2);
  }

  @DocAnnotation$annotation$(description = " Reject one or several received messages.\n")
  @TypeInfo("ceylon.language::Anything")
  public void basicNack(
    final @TypeInfo("ceylon.language::Integer") @Name("deliveryTag") long deliveryTag, 
    final @TypeInfo("ceylon.language::Boolean") @Name("multiple") boolean multiple, 
    final @TypeInfo("ceylon.language::Boolean") @Name("requeue") boolean requeue, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable|ceylon.json::Object)") @Name("resultHandler") Callable<?> resultHandler) {
    long arg_0 = deliveryTag;
    boolean arg_1 = multiple;
    boolean arg_2 = requeue;
    io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>> arg_3 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<io.vertx.core.json.JsonObject>(resultHandler) {
      public Object toCeylon(io.vertx.core.json.JsonObject event) {
        return io.vertx.lang.ceylon.ToCeylon.JsonObject.safeConvert(event);
      }
    };
    delegate.basicNack(arg_0, arg_1, arg_2, arg_3);
  }

  @DocAnnotation$annotation$(description = " Retrieve a message from a queue using AMQP.Basic.Get\n")
  @TypeInfo("ceylon.language::Anything")
  public void basicGet(
    final @TypeInfo("ceylon.language::String") @Name("queue") ceylon.language.String queue, 
    final @TypeInfo("ceylon.language::Boolean") @Name("autoAck") boolean autoAck, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable|ceylon.json::Object)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(queue);
    boolean arg_1 = autoAck;
    io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>> arg_2 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<io.vertx.core.json.JsonObject>(resultHandler) {
      public Object toCeylon(io.vertx.core.json.JsonObject event) {
        return io.vertx.lang.ceylon.ToCeylon.JsonObject.safeConvert(event);
      }
    };
    delegate.basicGet(arg_0, arg_1, arg_2);
  }

  @DocAnnotation$annotation$(description = " Start a non-nolocal, non-exclusive consumer, with auto acknowledgement and a server-generated consumerTag.\n")
  @TypeInfo("ceylon.language::Anything")
  public void basicConsume(
    final @TypeInfo("ceylon.language::String") @Name("queue") ceylon.language.String queue, 
    final @TypeInfo("ceylon.language::String") @Name("address") ceylon.language.String address, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(queue);
    java.lang.String arg_1 = io.vertx.lang.ceylon.ToJava.String.safeConvert(address);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_2 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.basicConsume(arg_0, arg_1, arg_2);
  }

  @DocAnnotation$annotation$(description = " Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag.\n")
  @TypeInfo("ceylon.language::Anything")
  public void basicConsume(
    final @TypeInfo("ceylon.language::String") @Name("queue") ceylon.language.String queue, 
    final @TypeInfo("ceylon.language::String") @Name("address") ceylon.language.String address, 
    final @TypeInfo("ceylon.language::Boolean") @Name("autoAck") boolean autoAck, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(queue);
    java.lang.String arg_1 = io.vertx.lang.ceylon.ToJava.String.safeConvert(address);
    boolean arg_2 = autoAck;
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_3 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.basicConsume(arg_0, arg_1, arg_2, arg_3);
  }

  @DocAnnotation$annotation$(description = " Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,\n which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.\n")
  @TypeInfo("ceylon.language::Anything")
  public void basicPublish(
    final @TypeInfo("ceylon.language::String") @Name("exchange") ceylon.language.String exchange, 
    final @TypeInfo("ceylon.language::String") @Name("routingKey") ceylon.language.String routingKey, 
    final @TypeInfo("ceylon.json::Object") @Name("message") ceylon.json.Object message, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(exchange);
    java.lang.String arg_1 = io.vertx.lang.ceylon.ToJava.String.safeConvert(routingKey);
    io.vertx.core.json.JsonObject arg_2 = io.vertx.lang.ceylon.ToJava.JsonObject.safeConvert(message);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_3 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.basicPublish(arg_0, arg_1, arg_2, arg_3);
  }

  @DocAnnotation$annotation$(description = " Request specific \"quality of service\" settings, Limiting the number of unacknowledged messages on\n a channel (or connection). This limit is applied separately to each new consumer on the channel.\n")
  @TypeInfo("ceylon.language::Anything")
  public void basicQos(
    final @TypeInfo("ceylon.language::Integer") @Name("prefetchCount") long prefetchCount, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    int arg_0 = (int)prefetchCount;
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_1 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.basicQos(arg_0, arg_1);
  }

  @DocAnnotation$annotation$(description = " Declare an exchange.\n")
  @TypeInfo("ceylon.language::Anything")
  public void exchangeDeclare(
    final @TypeInfo("ceylon.language::String") @Name("exchange") ceylon.language.String exchange, 
    final @TypeInfo("ceylon.language::String") @Name("type") ceylon.language.String type, 
    final @TypeInfo("ceylon.language::Boolean") @Name("durable") boolean durable, 
    final @TypeInfo("ceylon.language::Boolean") @Name("autoDelete") boolean autoDelete, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(exchange);
    java.lang.String arg_1 = io.vertx.lang.ceylon.ToJava.String.safeConvert(type);
    boolean arg_2 = durable;
    boolean arg_3 = autoDelete;
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_4 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.exchangeDeclare(arg_0, arg_1, arg_2, arg_3, arg_4);
  }

  @DocAnnotation$annotation$(description = " Declare an exchange with additional parameters such as dead lettering or an alternate exchnage.\n")
  @TypeInfo("ceylon.language::Anything")
  public void exchangeDeclare(
    final @TypeInfo("ceylon.language::String") @Name("exchange") ceylon.language.String exchange, 
    final @TypeInfo("ceylon.language::String") @Name("type") ceylon.language.String type, 
    final @TypeInfo("ceylon.language::Boolean") @Name("durable") boolean durable, 
    final @TypeInfo("ceylon.language::Boolean") @Name("autoDelete") boolean autoDelete, 
    final @TypeInfo("ceylon.language::Map<ceylon.language::String,ceylon.language::String>") @Name("config") ceylon.language.Map<ceylon.language.String,ceylon.language.String> config, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(exchange);
    java.lang.String arg_1 = io.vertx.lang.ceylon.ToJava.String.safeConvert(type);
    boolean arg_2 = durable;
    boolean arg_3 = autoDelete;
    java.util.Map<java.lang.String,java.lang.String> arg_4 = io.vertx.lang.ceylon.ToJava.convertMap(config, io.vertx.lang.ceylon.ToJava.String, io.vertx.lang.ceylon.ToJava.String);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_5 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.exchangeDeclare(arg_0, arg_1, arg_2, arg_3, arg_4, arg_5);
  }

  @DocAnnotation$annotation$(description = " Delete an exchange, without regard for whether it is in use or not.\n")
  @TypeInfo("ceylon.language::Anything")
  public void exchangeDelete(
    final @TypeInfo("ceylon.language::String") @Name("exchange") ceylon.language.String exchange, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(exchange);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_1 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.exchangeDelete(arg_0, arg_1);
  }

  @DocAnnotation$annotation$(description = " Bind an exchange to an exchange.\n")
  @TypeInfo("ceylon.language::Anything")
  public void exchangeBind(
    final @TypeInfo("ceylon.language::String") @Name("destination") ceylon.language.String destination, 
    final @TypeInfo("ceylon.language::String") @Name("source") ceylon.language.String source, 
    final @TypeInfo("ceylon.language::String") @Name("routingKey") ceylon.language.String routingKey, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(destination);
    java.lang.String arg_1 = io.vertx.lang.ceylon.ToJava.String.safeConvert(source);
    java.lang.String arg_2 = io.vertx.lang.ceylon.ToJava.String.safeConvert(routingKey);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_3 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.exchangeBind(arg_0, arg_1, arg_2, arg_3);
  }

  @DocAnnotation$annotation$(description = " Unbind an exchange from an exchange.\n")
  @TypeInfo("ceylon.language::Anything")
  public void exchangeUnbind(
    final @TypeInfo("ceylon.language::String") @Name("destination") ceylon.language.String destination, 
    final @TypeInfo("ceylon.language::String") @Name("source") ceylon.language.String source, 
    final @TypeInfo("ceylon.language::String") @Name("routingKey") ceylon.language.String routingKey, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(destination);
    java.lang.String arg_1 = io.vertx.lang.ceylon.ToJava.String.safeConvert(source);
    java.lang.String arg_2 = io.vertx.lang.ceylon.ToJava.String.safeConvert(routingKey);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_3 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.exchangeUnbind(arg_0, arg_1, arg_2, arg_3);
  }

  @DocAnnotation$annotation$(description = " Actively declare a server-named exclusive, autodelete, non-durable queue.\n")
  @TypeInfo("ceylon.language::Anything")
  public void queueDeclareAuto(
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable|ceylon.json::Object)") @Name("resultHandler") Callable<?> resultHandler) {
    io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>> arg_0 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<io.vertx.core.json.JsonObject>(resultHandler) {
      public Object toCeylon(io.vertx.core.json.JsonObject event) {
        return io.vertx.lang.ceylon.ToCeylon.JsonObject.safeConvert(event);
      }
    };
    delegate.queueDeclareAuto(arg_0);
  }

  @DocAnnotation$annotation$(description = " Declare a queue\n")
  @TypeInfo("ceylon.language::Anything")
  public void queueDeclare(
    final @TypeInfo("ceylon.language::String") @Name("queue") ceylon.language.String queue, 
    final @TypeInfo("ceylon.language::Boolean") @Name("durable") boolean durable, 
    final @TypeInfo("ceylon.language::Boolean") @Name("exclusive") boolean exclusive, 
    final @TypeInfo("ceylon.language::Boolean") @Name("autoDelete") boolean autoDelete, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable|ceylon.json::Object)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(queue);
    boolean arg_1 = durable;
    boolean arg_2 = exclusive;
    boolean arg_3 = autoDelete;
    io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>> arg_4 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<io.vertx.core.json.JsonObject>(resultHandler) {
      public Object toCeylon(io.vertx.core.json.JsonObject event) {
        return io.vertx.lang.ceylon.ToCeylon.JsonObject.safeConvert(event);
      }
    };
    delegate.queueDeclare(arg_0, arg_1, arg_2, arg_3, arg_4);
  }

  @DocAnnotation$annotation$(description = " Delete a queue, without regard for whether it is in use or has messages on it\n")
  @TypeInfo("ceylon.language::Anything")
  public void queueDelete(
    final @TypeInfo("ceylon.language::String") @Name("queue") ceylon.language.String queue, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable|ceylon.json::Object)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(queue);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>> arg_1 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<io.vertx.core.json.JsonObject>(resultHandler) {
      public Object toCeylon(io.vertx.core.json.JsonObject event) {
        return io.vertx.lang.ceylon.ToCeylon.JsonObject.safeConvert(event);
      }
    };
    delegate.queueDelete(arg_0, arg_1);
  }

  @DocAnnotation$annotation$(description = " Delete a queue\n")
  @TypeInfo("ceylon.language::Anything")
  public void queueDeleteIf(
    final @TypeInfo("ceylon.language::String") @Name("queue") ceylon.language.String queue, 
    final @TypeInfo("ceylon.language::Boolean") @Name("ifUnused") boolean ifUnused, 
    final @TypeInfo("ceylon.language::Boolean") @Name("ifEmpty") boolean ifEmpty, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable|ceylon.json::Object)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(queue);
    boolean arg_1 = ifUnused;
    boolean arg_2 = ifEmpty;
    io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>> arg_3 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<io.vertx.core.json.JsonObject>(resultHandler) {
      public Object toCeylon(io.vertx.core.json.JsonObject event) {
        return io.vertx.lang.ceylon.ToCeylon.JsonObject.safeConvert(event);
      }
    };
    delegate.queueDeleteIf(arg_0, arg_1, arg_2, arg_3);
  }

  @DocAnnotation$annotation$(description = " Bind a queue to an exchange\n")
  @TypeInfo("ceylon.language::Anything")
  public void queueBind(
    final @TypeInfo("ceylon.language::String") @Name("queue") ceylon.language.String queue, 
    final @TypeInfo("ceylon.language::String") @Name("exchange") ceylon.language.String exchange, 
    final @TypeInfo("ceylon.language::String") @Name("routingKey") ceylon.language.String routingKey, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(queue);
    java.lang.String arg_1 = io.vertx.lang.ceylon.ToJava.String.safeConvert(exchange);
    java.lang.String arg_2 = io.vertx.lang.ceylon.ToJava.String.safeConvert(routingKey);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_3 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.queueBind(arg_0, arg_1, arg_2, arg_3);
  }

  @DocAnnotation$annotation$(description = " Returns the number of messages in a queue ready to be delivered.\n")
  @TypeInfo("ceylon.language::Anything")
  public void messageCount(
    final @TypeInfo("ceylon.language::String") @Name("queue") ceylon.language.String queue, 
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable|ceylon.json::Object)") @Name("resultHandler") Callable<?> resultHandler) {
    java.lang.String arg_0 = io.vertx.lang.ceylon.ToJava.String.safeConvert(queue);
    io.vertx.core.Handler<io.vertx.core.AsyncResult<io.vertx.core.json.JsonObject>> arg_1 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<io.vertx.core.json.JsonObject>(resultHandler) {
      public Object toCeylon(io.vertx.core.json.JsonObject event) {
        return io.vertx.lang.ceylon.ToCeylon.JsonObject.safeConvert(event);
      }
    };
    delegate.messageCount(arg_0, arg_1);
  }

  @DocAnnotation$annotation$(description = " Start the rabbitMQ client. Create the connection and the chanel.\n")
  @TypeInfo("ceylon.language::Anything")
  public void start(
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_0 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.start(arg_0);
  }

  @DocAnnotation$annotation$(description = " Stop the rabbitMQ client. Close the connection and its chanel.\n")
  @TypeInfo("ceylon.language::Anything")
  public void stop(
    final @TypeInfo("ceylon.language::Anything(ceylon.language::Throwable?)") @Name("resultHandler") Callable<?> resultHandler) {
    io.vertx.core.Handler<io.vertx.core.AsyncResult<java.lang.Void>> arg_0 = resultHandler == null ? null : new io.vertx.lang.ceylon.CallableAsyncResultHandler<java.lang.Void>(resultHandler) {
      public Object toCeylon(java.lang.Void event) {
        return null;
      }
    };
    delegate.stop(arg_0);
  }

  @DocAnnotation$annotation$(description = " Check if a connection is open\n")
  @TypeInfo("ceylon.language::Boolean")
  public boolean $isConnected() {
    boolean ret = delegate.isConnected();
    return ret;
  }

  @DocAnnotation$annotation$(description = " Check if a channel is open\n")
  @TypeInfo("ceylon.language::Boolean")
  public boolean $isOpenChannel() {
    boolean ret = delegate.isOpenChannel();
    return ret;
  }

}
