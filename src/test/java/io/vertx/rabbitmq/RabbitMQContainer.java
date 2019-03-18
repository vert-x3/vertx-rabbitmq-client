package io.vertx.rabbitmq;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.rabbitmq.http.client.Client;
import org.testcontainers.containers.GenericContainer;

import java.util.function.Consumer;

public class RabbitMQContainer extends GenericContainer {
  private Integer managementPort = 15672;
  private Integer connectionPort = 5672;
  private String defaultVHost = "/";

  RabbitMQContainer() {
    super("rabbitmq:3-management");
    withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd -> cmd.withHostName("my-rabbit"));
    withExposedPorts(managementPort, connectionPort);
  }

  public Client getHttpClient() throws Exception {
    String url = new StringBuilder()
      .append("http://")
      .append(this.getContainerIpAddress())
      .append(":")
      .append(this.getMappedPort(managementPort))
      .append("/api/")
      .toString();
    return new Client(url, "guest", "guest");
  }

  public String getDefaultVHost() {
    return defaultVHost;
  }
}


