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
	private NetServer proxyServer;
	private NetClient proxyClient;

	private void startProxy() throws Exception {
		CompletableFuture<Void> latch = new CompletableFuture<>();
		RabbitMQOptions config = super.config();
		ConnectionFactory cf = new ConnectionFactory();
		NetClientOptions clientOptions = new NetClientOptions();
		cf.setUri(config.getUri());
		String host = cf.getHost();
		int port = cf.getPort();
		NetServerOptions serverOptions = new NetServerOptions();
		serverOptions.setSsl(true);
		// TODO
//		serverOptions.setKeyStoreOptions(new JksOptions().setPath(KEYSTORE).setPassword(PASSWORD));
		proxyClient = vertx.createNetClient(clientOptions);
		proxyServer = vertx.createNetServer(serverOptions).connectHandler(serverSocket -> {
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
					serverSocket.close();
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

	@Override
	public RabbitMQOptions config() throws Exception {
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
				.setConnectionRetryDelay(connectionRetryDelay);
	}

	@Test
	public void testConnectionWithSSL() throws Exception {
		startProxy();
		try {
			connect();
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testConnectionWithoutSSL() throws Exception {
		startProxy();
		try {
			connect();
			fail();
		} catch (Exception e) {
			// Expected
		}
	}
}
