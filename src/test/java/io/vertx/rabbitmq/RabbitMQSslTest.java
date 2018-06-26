package io.vertx.rabbitmq;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

import org.junit.Test;

import com.rabbitmq.client.ConnectionFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

public class RabbitMQSslTest extends RabbitMQClientTestBase {

	private static final int PROXY_PORT = 8000;

	protected Integer connectionRetries = RabbitMQOptions.DEFAULT_CONNECTION_RETRIES;
	protected long connectionRetryDelay = RabbitMQOptions.DEFAULT_CONNECTION_RETRY_DELAY;
	private NetServer proxyServer;
	private NetClient proxyClient;

	/**
	 * Start a server that will accept AMQP SSL connections.
	 * 
	 * @throws Exception
	 *             when setting up the server fails
	 */
	private void startServer() throws Exception {
		CompletableFuture<Void> latch = new CompletableFuture<>();
		RabbitMQOptions config = super.config();
		ConnectionFactory cf = new ConnectionFactory();
		NetClientOptions clientOptions = new NetClientOptions();
		if (config.getUri() != null) {
			cf.setUri(config.getUri());
			if (cf.isSSL()) {
				clientOptions.setSsl(true);
				clientOptions.setTrustAll(true);
			}
		} else {
			cf.setPort(config.getPort());
			cf.setHost(config.getHost());
		}
		String host = cf.getHost();
		int port = cf.getPort();
		proxyClient = vertx.createNetClient(clientOptions);
		cf.setUri(config.getUri());
		NetServerOptions serverOptions = new NetServerOptions();
		serverOptions.setSsl(true);
		serverOptions.setKeyStoreOptions(new JksOptions().setPath("tls/server-keystore.jks").setPassword("wibble"));
		proxyServer = vertx.createNetServer(serverOptions).connectHandler(serverSocket -> {
			System.out.println(serverSocket.isSsl());
			serverSocket.pause();
	        proxyClient.connect(port, host, ar -> {
	          serverSocket.resume();
	          if (ar.succeeded()) {
	            NetSocket clientSocket = ar.result();
	            serverSocket.handler(clientSocket::write);
	            serverSocket.exceptionHandler(err -> serverSocket.close());
	            serverSocket.closeHandler(v -> clientSocket.close());
	            clientSocket.handler(serverSocket::write);
	            clientSocket.exceptionHandler(err -> clientSocket.close());
	            clientSocket.closeHandler(v -> serverSocket.close());
	          } else {
	            serverSocket.close();;
	          }
	        });
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
		if (proxyServer != null) {
			proxyServer.close();
		}
		if (proxyClient != null) {
			proxyClient.close();
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
	 * 
	 * @param useSsl
	 *            whether the connection is encrypted using SSL or not
	 * @throws Exception
	 *             when the connection can not be started
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
				System.out.println(client.isConnected());
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
	 * 
	 * @throws Exception
	 *             when something goes wrong
	 */
	@Test
	public void testConnectionWithSSL() throws Exception {
		startServer();
		try {
			System.out.println("with SSL");
			connect(true);
		} catch (Exception e) {
			fail();
		}
	}

	/**
	 * Try connecting a client that does not use SSL
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConnectionWithoutSSL() throws Exception {
		startServer();
		try {
			System.out.println("without SSL");
			connect(false);
			fail();
		} catch (Exception e) {
			// Expected
		}
	}
}
