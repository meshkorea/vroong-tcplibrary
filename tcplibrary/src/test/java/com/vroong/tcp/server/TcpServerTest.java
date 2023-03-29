package com.vroong.tcp.server;

import com.vroong.tcp.TestHelper;
import com.vroong.tcp.client.DisposableTcpClient;
import com.vroong.tcp.client.PooledTcpClient;
import com.vroong.tcp.client.PooledTcpClient.Tuple;
import com.vroong.tcp.client.TcpClient;
import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.config.TcpClientProperties.Pool;
import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import com.vroong.tcp.message.strategy.NoOpHeaderStrategy;
import com.vroong.tcp.server.example.EchoServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.vroong.tcp.config.VroongTcpConstants.DEFAULT_CHARSET;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TcpServerTest {

  // For difference betwee ms949 vs cp949
  // see https://devday.tistory.com/entry/MS949-vs-Cp949
  final String message = "가나다라 똠방각하";

  TestHelper libraryTest = new TestHelper();
  TcpServerProperties serverProperties = libraryTest.getServerProperties();
  TcpClientProperties clientProperties = libraryTest.getClientProperties();
  Pool poolConfig = clientProperties.getPool();

  @Test
  void whenKeepConnectionIsFalse() throws Exception {
    final HeaderStrategy strategy = new NoOpHeaderStrategy(DEFAULT_CHARSET);
    final TcpServer server = new EchoServer(serverProperties, strategy);
    libraryTest.start(server);

    final TcpClient client = new DisposableTcpClient(clientProperties, strategy);
    final String received = client.send(message);

    assertEquals(message, received);
  }

  @Test
  void whenKeepConnectionIsTrue() {
    final HeaderStrategy strategy = new NoOpHeaderStrategy(DEFAULT_CHARSET);
    final TcpServer server = new EchoServer(serverProperties, strategy);
    libraryTest.startWithKeepalive(server);

    final TcpClient client = new PooledTcpClient(clientProperties, strategy);
    final ObjectPool<Tuple> pool = libraryTest.getPoolFrom(client);
    final int noOfTests = poolConfig.getMinIdle();
    final Executor executor = Executors.newFixedThreadPool(noOfTests);

    final List<CompletableFuture<String>> futures = new ArrayList<>();
    for (int i = 0; i < noOfTests; i++) {
      final CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
        try {
          log.info("Sending message in a thread, pool state: numIdle={}, numActive={}",
              pool.getNumIdle(),
              pool.getNumActive());
          return client.send(message);
        } catch (Exception e) {
          log.error(e.getMessage());
        }
        return "";
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
            log.info("response: {}", f.get());
          } catch (Exception ignored) {
          }
        });

    log.info("All done, pool state: numIdle={}, numActive={}", pool.getNumIdle(),
        pool.getNumActive());

    libraryTest.stop(server);
  }
}
