package com.vroong.tcp;

import static com.vroong.tcp.config.VroongTcpConstants.PROJECT_ROOT;

import com.vroong.tcp.client.PooledTcpClient;
import com.vroong.tcp.client.TcpClient;
import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.config.TcpServerProperties;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.vroong.tcp.server.AbstractTcpServer;
import com.vroong.tcp.server.TcpServer;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.pool2.ObjectPool;
import org.junit.platform.commons.util.ReflectionUtils;
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

  public void startWithKeepalive(TcpServer server) {
    startInternal(server, true);
  }

  public void start(TcpServer server) {
    startInternal(server, false);
  }

  @SneakyThrows
  void startInternal(TcpServer server, boolean keepConnection) {
    final AbstractTcpServer abstractTcpServer = (AbstractTcpServer) server;
    final ExecutorService es = Executors.newSingleThreadExecutor();
    es.execute(() -> {
      try {
        abstractTcpServer.setKeepConnection(keepConnection);
        abstractTcpServer.start();
      } catch (Exception ignored) {
      }
    });

    es.awaitTermination(3, TimeUnit.SECONDS);
    es.shutdown();
  }

  @SneakyThrows
  public void stop(TcpServer server) {
    TimeUnit.SECONDS.sleep(3);
    try {
      server.stop();
    } catch (Exception ignored) {
    }
  }

  public ObjectPool<PooledTcpClient.Tuple> getPoolFrom(TcpClient client) {
    // private method에 접근하기 위해 Reflection 사용
    final Method method = ReflectionUtils.findMethod(client.getClass(), "getPool").get();
    return (ObjectPool<PooledTcpClient.Tuple>) ReflectionUtils.invokeMethod(method, client);
  }
}
