package io.vertx.rabbitmq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.rabbitmq.client.ConnectionFactory;

import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

public class RabbitMQSslTest extends RabbitMQClientTestBase {

	private static final int PROXY_PORT = 8000;

	private static final String PASSWORD = "password";
	private static final String KEYSTORE = "src/test/resources/broker.keystore";

	protected Integer connectionRetries = RabbitMQOptions.DEFAULT_CONNECTION_RETRIES;
	protected long connectionRetryDelay = RabbitMQOptions.DEFAULT_CONNECTION_RETRY_DELAY;
	private NetServer server;

	/**
	 * Start a server that will accept AMQP SSL connections.
	 * @throws Exception when setting up the server fails
	 */
	private void startServer() throws Exception {
		CompletableFuture<Void> latch = new CompletableFuture<>();
		RabbitMQOptions config = super.config();
		ConnectionFactory cf = new ConnectionFactory();
		cf.setUri(config.getUri());
		NetServerOptions serverOptions = new NetServerOptions();
		serverOptions.setSsl(true);
		// TODO
//		serverOptions.setKeyStoreOptions(new JksOptions().setPath(KEYSTORE).setPassword(PASSWORD));
		server = vertx.createNetServer(serverOptions).connectHandler(serverSocket -> {
			// TODO do we need logic here?
		}).listen(PROXY_PORT, "localhost", ar -> {
			if (ar.succeeded()) {
				latch.complete(null);
			} else {
				latch.completeExceptionally(ar.cause());
			}
		});
		latch.get(10, TimeUnit.SECONDS);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (server != null) {
			server.close();
		}
	}

	private RabbitMQOptions config(boolean useSsl) throws Exception {
		RabbitMQOptions cfg = super.config();
		String username;
		String password;
		String vhost;
		if (cfg.getUri() != null) {
			ConnectionFactory cf = new ConnectionFactory();
			cf.setUri(cfg.getUri());
			username = cf.getUsername();
			password = cf.getPassword();
			vhost = "/" + cf.getVirtualHost();
		} else {
			username = "guest";
			password = "guest";
			vhost = "";
		}
		String uri = "amqp://" + username + ":" + password + "@localhost:" + PROXY_PORT + vhost;
		return new RabbitMQOptions().setUri(uri).setConnectionRetries(connectionRetries)
				.setConnectionRetryDelay(connectionRetryDelay).setSsl(useSsl);
	}
	
	/**
	 * Try connecting a new RabbitMQClient to the server
	 * @param useSsl whether the connection is encrypted using SSL or not
	 * @throws Exception when the connection can not be started
	 */
	private void connect(boolean useSsl) throws Exception {
	if (client != null) {
	      throw new IllegalStateException("Client already started");
	    }
	    RabbitMQOptions config = config(useSsl);
	    client = RabbitMQClient.create(vertx, config);
	    CompletableFuture<Void> latch = new CompletableFuture<>();
	    client.start(ar -> {
	      if (ar.succeeded()) {
	        latch.complete(null);
	      } else {
	        latch.completeExceptionally(ar.cause());
	      }
	    });
	    latch.get(10L, TimeUnit.SECONDS);
	    ConnectionFactory factory = new ConnectionFactory();
	    if (config.getUri() != null) {
	      factory.setUri(config.getUri());
	    }
	    channel = factory.newConnection().createChannel();
	}
	
	/**
	 * Try connecting a client that uses SSL
	 * @throws Exception when something goes wrong
	 */
	@Test
	public void testConnectionWithSSL() throws Exception {
		startServer();
		try {
			connect(true);
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Try connecting a client that does not use SSL
	 * @throws Exception
	 */
	@Test
	public void testConnectionWithoutSSL() throws Exception {
		startServer();
		try {
			connect(false);
			fail();
		} catch (Exception e) {
			// Expected
		}
	}
}
