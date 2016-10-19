require 'vertx/vertx'
require 'vertx/util/utils.rb'
# Generated from io.vertx.rabbitmq.RabbitMQClient
module VertxRabbitmq
  class RabbitMQClient
    # @private
    # @param j_del [::VertxRabbitmq::RabbitMQClient] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxRabbitmq::RabbitMQClient] the underlying java delegate
    def j_del
      @j_del
    end
    # @param [::Vertx::Vertx] vertx 
    # @param [Hash{String => Object}] config 
    # @return [::VertxRabbitmq::RabbitMQClient]
    def self.create(vertx=nil,config=nil)
      if vertx.class.method_defined?(:j_del) && config.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxRabbitmq::RabbitMQClient.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCoreJson::JsonObject.java_class]).call(vertx.j_del,::Vertx::Util::Utils.to_json_object(config)),::VertxRabbitmq::RabbitMQClient)
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,config)"
    end
    #  Acknowledge one or several received messages. Supply the deliveryTag from the AMQP.Basic.GetOk or AMQP.Basic.Deliver
    #  method containing the received message being acknowledged.
    # @param [Fixnum] deliveryTag 
    # @param [true,false] multiple 
    # @yield 
    # @return [void]
    def basic_ack(deliveryTag=nil,multiple=nil)
      if deliveryTag.class == Fixnum && (multiple.class == TrueClass || multiple.class == FalseClass) && block_given?
        return @j_del.java_method(:basicAck, [Java::long.java_class,Java::boolean.java_class,Java::IoVertxCore::Handler.java_class]).call(deliveryTag,multiple,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling basic_ack(deliveryTag,multiple)"
    end
    #  Reject one or several received messages.
    # @param [Fixnum] deliveryTag 
    # @param [true,false] multiple 
    # @param [true,false] requeue 
    # @yield 
    # @return [void]
    def basic_nack(deliveryTag=nil,multiple=nil,requeue=nil)
      if deliveryTag.class == Fixnum && (multiple.class == TrueClass || multiple.class == FalseClass) && (requeue.class == TrueClass || requeue.class == FalseClass) && block_given?
        return @j_del.java_method(:basicNack, [Java::long.java_class,Java::boolean.java_class,Java::boolean.java_class,Java::IoVertxCore::Handler.java_class]).call(deliveryTag,multiple,requeue,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling basic_nack(deliveryTag,multiple,requeue)"
    end
    #  Retrieve a message from a queue using AMQP.Basic.Get
    # @param [String] queue 
    # @param [true,false] autoAck 
    # @yield 
    # @return [void]
    def basic_get(queue=nil,autoAck=nil)
      if queue.class == String && (autoAck.class == TrueClass || autoAck.class == FalseClass) && block_given?
        return @j_del.java_method(:basicGet, [Java::java.lang.String.java_class,Java::boolean.java_class,Java::IoVertxCore::Handler.java_class]).call(queue,autoAck,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling basic_get(queue,autoAck)"
    end
    #  Start a non-nolocal, non-exclusive consumer, with a server-generated consumerTag.
    # @param [String] queue 
    # @param [String] address 
    # @param [true,false] autoAck 
    # @yield 
    # @return [void]
    def basic_consume(queue=nil,address=nil,autoAck=nil)
      if queue.class == String && address.class == String && block_given? && autoAck == nil
        return @j_del.java_method(:basicConsume, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(queue,address,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      elsif queue.class == String && address.class == String && (autoAck.class == TrueClass || autoAck.class == FalseClass) && block_given?
        return @j_del.java_method(:basicConsume, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::boolean.java_class,Java::IoVertxCore::Handler.java_class]).call(queue,address,autoAck,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling basic_consume(queue,address,autoAck)"
    end
    #  Publish a message. Publishing to a non-existent exchange will result in a channel-level protocol exception,
    #  which closes the channel. Invocations of Channel#basicPublish will eventually block if a resource-driven alarm is in effect.
    # @param [String] exchange 
    # @param [String] routingKey 
    # @param [Hash{String => Object}] message 
    # @yield 
    # @return [void]
    def basic_publish(exchange=nil,routingKey=nil,message=nil)
      if exchange.class == String && routingKey.class == String && message.class == Hash && block_given?
        return @j_del.java_method(:basicPublish, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(exchange,routingKey,::Vertx::Util::Utils.to_json_object(message),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling basic_publish(exchange,routingKey,message)"
    end
    #  Request specific "quality of service" settings, Limiting the number of unacknowledged messages on
    #  a channel (or connection). This limit is applied separately to each new consumer on the channel.
    # @param [Fixnum] prefetchCount 
    # @yield 
    # @return [void]
    def basic_qos(prefetchCount=nil)
      if prefetchCount.class == Fixnum && block_given?
        return @j_del.java_method(:basicQos, [Java::int.java_class,Java::IoVertxCore::Handler.java_class]).call(prefetchCount,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling basic_qos(prefetchCount)"
    end
    #  Declare an exchange with additional parameters such as dead lettering or an alternate exchnage.
    # @param [String] exchange 
    # @param [String] type 
    # @param [true,false] durable 
    # @param [true,false] autoDelete 
    # @param [Hash{String => String}] config 
    # @yield 
    # @return [void]
    def exchange_declare(exchange=nil,type=nil,durable=nil,autoDelete=nil,config=nil)
      if exchange.class == String && type.class == String && (durable.class == TrueClass || durable.class == FalseClass) && (autoDelete.class == TrueClass || autoDelete.class == FalseClass) && block_given? && config == nil
        return @j_del.java_method(:exchangeDeclare, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::boolean.java_class,Java::boolean.java_class,Java::IoVertxCore::Handler.java_class]).call(exchange,type,durable,autoDelete,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      elsif exchange.class == String && type.class == String && (durable.class == TrueClass || durable.class == FalseClass) && (autoDelete.class == TrueClass || autoDelete.class == FalseClass) && config.class == Hash && block_given?
        return @j_del.java_method(:exchangeDeclare, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::boolean.java_class,Java::boolean.java_class,Java::JavaUtil::Map.java_class,Java::IoVertxCore::Handler.java_class]).call(exchange,type,durable,autoDelete,Hash[config.map { |k,v| [k,v] }],(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling exchange_declare(exchange,type,durable,autoDelete,config)"
    end
    #  Delete an exchange, without regard for whether it is in use or not.
    # @param [String] exchange 
    # @yield 
    # @return [void]
    def exchange_delete(exchange=nil)
      if exchange.class == String && block_given?
        return @j_del.java_method(:exchangeDelete, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(exchange,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling exchange_delete(exchange)"
    end
    #  Bind an exchange to an exchange.
    # @param [String] destination 
    # @param [String] source 
    # @param [String] routingKey 
    # @yield 
    # @return [void]
    def exchange_bind(destination=nil,source=nil,routingKey=nil)
      if destination.class == String && source.class == String && routingKey.class == String && block_given?
        return @j_del.java_method(:exchangeBind, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(destination,source,routingKey,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling exchange_bind(destination,source,routingKey)"
    end
    #  Unbind an exchange from an exchange.
    # @param [String] destination 
    # @param [String] source 
    # @param [String] routingKey 
    # @yield 
    # @return [void]
    def exchange_unbind(destination=nil,source=nil,routingKey=nil)
      if destination.class == String && source.class == String && routingKey.class == String && block_given?
        return @j_del.java_method(:exchangeUnbind, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(destination,source,routingKey,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling exchange_unbind(destination,source,routingKey)"
    end
    #  Actively declare a server-named exclusive, autodelete, non-durable queue.
    # @yield 
    # @return [void]
    def queue_declare_auto
      if block_given?
        return @j_del.java_method(:queueDeclareAuto, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling queue_declare_auto()"
    end
    #  Declare a queue
    # @param [String] queue 
    # @param [true,false] durable 
    # @param [true,false] exclusive 
    # @param [true,false] autoDelete 
    # @yield 
    # @return [void]
    def queue_declare(queue=nil,durable=nil,exclusive=nil,autoDelete=nil)
      if queue.class == String && (durable.class == TrueClass || durable.class == FalseClass) && (exclusive.class == TrueClass || exclusive.class == FalseClass) && (autoDelete.class == TrueClass || autoDelete.class == FalseClass) && block_given?
        return @j_del.java_method(:queueDeclare, [Java::java.lang.String.java_class,Java::boolean.java_class,Java::boolean.java_class,Java::boolean.java_class,Java::IoVertxCore::Handler.java_class]).call(queue,durable,exclusive,autoDelete,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling queue_declare(queue,durable,exclusive,autoDelete)"
    end
    #  Delete a queue, without regard for whether it is in use or has messages on it
    # @param [String] queue 
    # @yield 
    # @return [void]
    def queue_delete(queue=nil)
      if queue.class == String && block_given?
        return @j_del.java_method(:queueDelete, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(queue,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling queue_delete(queue)"
    end
    #  Delete a queue
    # @param [String] queue 
    # @param [true,false] ifUnused 
    # @param [true,false] ifEmpty 
    # @yield 
    # @return [void]
    def queue_delete_if(queue=nil,ifUnused=nil,ifEmpty=nil)
      if queue.class == String && (ifUnused.class == TrueClass || ifUnused.class == FalseClass) && (ifEmpty.class == TrueClass || ifEmpty.class == FalseClass) && block_given?
        return @j_del.java_method(:queueDeleteIf, [Java::java.lang.String.java_class,Java::boolean.java_class,Java::boolean.java_class,Java::IoVertxCore::Handler.java_class]).call(queue,ifUnused,ifEmpty,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling queue_delete_if(queue,ifUnused,ifEmpty)"
    end
    #  Bind a queue to an exchange
    # @param [String] queue 
    # @param [String] exchange 
    # @param [String] routingKey 
    # @yield 
    # @return [void]
    def queue_bind(queue=nil,exchange=nil,routingKey=nil)
      if queue.class == String && exchange.class == String && routingKey.class == String && block_given?
        return @j_del.java_method(:queueBind, [Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(queue,exchange,routingKey,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling queue_bind(queue,exchange,routingKey)"
    end
    #  Returns the number of messages in a queue ready to be delivered.
    # @param [String] queue 
    # @yield 
    # @return [void]
    def message_count(queue=nil)
      if queue.class == String && block_given?
        return @j_del.java_method(:messageCount, [Java::java.lang.String.java_class,Java::IoVertxCore::Handler.java_class]).call(queue,(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling message_count(queue)"
    end
    #  Start the rabbitMQ client. Create the connection and the chanel.
    # @yield 
    # @return [void]
    def start
      if block_given?
        return @j_del.java_method(:start, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling start()"
    end
    #  Stop the rabbitMQ client. Close the connection and its chanel.
    # @yield 
    # @return [void]
    def stop
      if block_given?
        return @j_del.java_method(:stop, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling stop()"
    end
    #  Check if a connection is open
    # @return [true,false] true when the connection is open, false otherwise
    def connected?
      if !block_given?
        return @j_del.java_method(:isConnected, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling connected?()"
    end
    #  Check if a channel is open
    # @return [true,false] true when the connection is open, false otherwise
    def open_channel?
      if !block_given?
        return @j_del.java_method(:isOpenChannel, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling open_channel?()"
    end
  end
end
