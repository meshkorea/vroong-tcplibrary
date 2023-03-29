package com.vroong.tcp.client;

import com.vroong.tcp.TestHelper;
import com.vroong.tcp.client.PooledTcpClient.Tuple;
import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.config.TcpClientProperties.Pool;
import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import com.vroong.tcp.message.strategy.NoOpHeaderStrategy;
import com.vroong.tcp.server.TcpServer;
import com.vroong.tcp.server.example.EchoServerWithTLS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.vroong.tcp.config.VroongTcpConstants.DEFAULT_CHARSET;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class TcpClientWithTLSTest {

  TestHelper libraryTest = new TestHelper();

  TcpServerProperties serverProperties = libraryTest.getServerProperties();
  TcpClientProperties clientProperties = libraryTest.getClientProperties();
  Pool poolConfig = clientProperties.getPool();

  @ParameterizedTest
  @ValueSource(strings = {"utf-8", "euc-kr", "cp949", "ms949"})
  void disposableTcpClient(String charsetName) throws Exception {
    final HeaderStrategy strategy = new NoOpHeaderStrategy(Charset.forName(charsetName));
    final TcpServer server = new EchoServerWithTLS(serverProperties, strategy);
    libraryTest.start(server);

    final TcpClient client = new DisposableTcpClient(clientProperties, strategy, true);

    final String message = "안녕하세요?";
    final String response = client.send(message);

    assertEquals(message, response);
    log.info("response: {}", response);

    libraryTest.stop(server);
  }

  @ParameterizedTest
  @ValueSource(strings = {"utf-8", "euc-kr", "cp949", "ms949"})
  void pooledTcpClient(String charsetName) throws Exception {
    final HeaderStrategy strategy = new NoOpHeaderStrategy(Charset.forName(charsetName));
    final TcpServer server = new EchoServerWithTLS(serverProperties, strategy);
    libraryTest.start(server);

    final TcpClient client = new PooledTcpClient(clientProperties, strategy, true);

    final ObjectPool<Tuple> pool = libraryTest.getPoolFrom(client);
    assertEquals(poolConfig.getMinIdle(), pool.getNumIdle());

    final String message = "안녕하세요?";
    final String response = client.send(message);

    assertEquals(message, response);
    assertEquals(poolConfig.getMinIdle(), pool.getNumIdle());
    assertEquals(0, pool.getNumActive());
    log.info("response: {}", response);

    libraryTest.stop(server);
  }

  @Test
    // 동시성 문제가 있지만, 완전 못쓸 수준을 아니라 판단함
    // spring-data-redis 등도 apache.commons.pool2를 사용함
  void pooledTcpClient_underMultiThreads() {
    final HeaderStrategy strategy = new NoOpHeaderStrategy(DEFAULT_CHARSET);
    final TcpServer server = new EchoServerWithTLS(serverProperties, strategy);
    libraryTest.start(server);

    final TcpClient client = new PooledTcpClient(clientProperties, strategy, true);
    final ObjectPool<Tuple> pool = libraryTest.getPoolFrom(client);
    final int noOfTests = poolConfig.getMinIdle();
    final Executor executor = Executors.newFixedThreadPool(noOfTests);
    final String message = "안녕하세요?";

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
