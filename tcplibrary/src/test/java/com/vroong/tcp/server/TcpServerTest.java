package com.vroong.tcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.vroong.tcp.TestHelper;
import com.vroong.tcp.client.DisposableTcpClient;
import com.vroong.tcp.client.PooledTcpClient;
import com.vroong.tcp.client.PooledTcpClient.Tuple;
import com.vroong.tcp.client.TcpClient;
import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.config.TcpClientProperties.Pool;
import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.server.example.EchoServer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

@Slf4j
class TcpServerTest {

  final String message = "안녕하세요?";

  TestHelper libraryTest = new TestHelper();
  TcpServerProperties serverProperties = libraryTest.getServerProperties();
  TcpClientProperties clientProperties = libraryTest.getClientProperties();
  Pool poolConfig = clientProperties.getPool();

  EchoServer server = new EchoServer(serverProperties);

  @Test
  void whenKeepConnectionIsFalse() throws Exception {
    startServer(false);
    final TcpClient client = new DisposableTcpClient(clientProperties);
    final byte[] received = client.send(message.getBytes());
    assertEquals(message, new String(received));
  }

  @Test
  void whenKeepConnectionIsTrue() {
    startServer(true);

    final TcpClient client = new PooledTcpClient(clientProperties);
    final ObjectPool<Tuple> pool = getPool(client);
    final int noOfTests = poolConfig.getMinIdle();
    final Executor executor = Executors.newFixedThreadPool(noOfTests);

    final List<CompletableFuture<byte[]>> futures = new ArrayList<>();
    for (int i = 0; i < noOfTests; i++) {
      final CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
        try {
          log.info("Sending message in a thread, pool state: numIdle={}, numActive={}",
              pool.getNumIdle(),
              pool.getNumActive());
          return client.send(message.getBytes());
        } catch (Exception e) {
          log.error(e.getMessage());
        }
        return new byte[]{};
      }, executor);

      futures.add(future);
    }

    while (true) {
      final boolean allDone = futures.stream().allMatch(Future::isDone);
      if (allDone) {
        break;
      }
    }

    futures
        .forEach(f -> {
          try {
            log.info("response: {}", new String(f.get()));
          } catch (Exception ignored) {
          }
        });

    log.info("All done, pool state: numIdle={}, numActive={}", pool.getNumIdle(),
        pool.getNumActive());
  }

  ObjectPool<Tuple> getPool(TcpClient client) {
    // private method에 접근하기 위해 Reflection 사용
    final Method method = ReflectionUtils.findMethod(client.getClass(), "getPool").get();
    return (ObjectPool<Tuple>) ReflectionUtils.invokeMethod(method, client);
  }

  @SneakyThrows
  void startServer(boolean keepConnection) {
    new Thread(() -> {
      try {
        server.setKeepConnection(keepConnection);
        server.start();
      } catch (Exception ignored) {
      }
    }).start();

    TimeUnit.SECONDS.sleep(3);
  }

  @SneakyThrows
  @AfterEach
  void shutDown() {
    TimeUnit.SECONDS.sleep(3);
    server.stop();
  }
}
