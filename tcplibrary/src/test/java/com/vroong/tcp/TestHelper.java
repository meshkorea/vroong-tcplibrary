package com.vroong.tcp;

import static com.vroong.tcp.config.VroongTcpConstants.PROJECT_ROOT;

import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.config.TcpServerProperties;
import java.io.InputStream;
import lombok.Data;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

@Getter
public class TestHelper {

  protected TcpServerProperties serverProperties;
  protected TcpClientProperties clientProperties;

  public TestHelper() {
    final Yaml yaml = new Yaml();
    final InputStream inputStream = TestHelper.class
        .getClassLoader()
        .getResourceAsStream("application.yml");

    final String stream = TcpUtils.inputStreamToString(inputStream)
        .replaceAll("\\$\\{user.dir}", PROJECT_ROOT);

    final TcpProperties tcpProperties = yaml.loadAs(stream, TcpProperties.class);
    serverProperties = tcpProperties.getTcp().getServer();
    clientProperties = tcpProperties.getTcp().getClient();
  }

  @Data
  public static class TcpProperties {
    Tcp tcp = new Tcp();

    @Data
    public static class Tcp {
      TcpServerProperties server = new TcpServerProperties();
      TcpClientProperties client = new TcpClientProperties();
    }
  }
}
