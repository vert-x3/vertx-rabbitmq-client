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
import io.vertx.ceylon.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@Ceylon(major = 8)
@Name("rabbitMQClient")
@com.redhat.ceylon.compiler.java.metadata.Object
public class rabbitMQClient_ implements ReifiedType {

  private static final rabbitMQClient_ instance = new rabbitMQClient_();
  public static final TypeDescriptor $TypeDescriptor$ = TypeDescriptor.klass(rabbitMQClient_.class);

  @Ignore
  public TypeDescriptor $getType$() {
    return $TypeDescriptor$;
  }

  @Ignore
  @TypeInfo("io.vertx.ceylon.rabbitmq::rabbitMQClient")
  public static rabbitMQClient_ get_() {
    return instance;
  }


  @TypeInfo("io.vertx.ceylon.rabbitmq::RabbitMQClient")
  public RabbitMQClient create(
    final @TypeInfo("io.vertx.ceylon.core::Vertx") @Name("vertx")  Vertx vertx, 
    final @TypeInfo("ceylon.json::Object") @Name("config")  ceylon.json.Object config) {
    io.vertx.core.Vertx arg_0 = io.vertx.ceylon.core.Vertx.TO_JAVA.safeConvert(vertx);
    io.vertx.core.json.JsonObject arg_1 = io.vertx.lang.ceylon.ToJava.JsonObject.safeConvert(config);
    RabbitMQClient ret = io.vertx.ceylon.rabbitmq.RabbitMQClient.TO_CEYLON.converter().safeConvert(io.vertx.rabbitmq.RabbitMQClient.create(arg_0, arg_1));
    return ret;
  }

}
