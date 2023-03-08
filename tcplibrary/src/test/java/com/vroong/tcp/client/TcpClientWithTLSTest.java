package com.vroong.tcp.client;

import static com.vroong.tcp.config.VroongTcpConstants.DEFAULT_CHARSET;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.vroong.tcp.TestHelper;
import com.vroong.tcp.client.PooledTcpClient.Tuple;
import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.config.TcpClientProperties.Pool;
import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.NullHeaderStrategy;
import com.vroong.tcp.server.example.EchoServerWithTLS;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.ReflectionUtils;

@Slf4j
@TestInstance(Lifecycle.PER_CLASS)
class TcpClientWithTLSTest {

  TestHelper libraryTest = new TestHelper();

  TcpServerProperties serverProperties = libraryTest.getServerProperties();
  TcpClientProperties clientProperties = libraryTest.getClientProperties();
  Pool poolConfig = clientProperties.getPool();

  EchoServerWithTLS server = new EchoServerWithTLS(serverProperties);

  @ParameterizedTest
  @ValueSource(strings = {"utf-8", "euc-kr", "cp949"})
  void disposableTcpClient(String charsetName) throws Exception {
    final Charset charset = Charset.forName(charsetName);

    final TcpClient client = new DisposableTcpClient(clientProperties, new NullHeaderStrategy(), true);

    final String message = "안녕하세요?";
    final byte[] response = client.send(message.getBytes(charset));

    assertEquals(message, new String(response, charset));
  }

  @ParameterizedTest
  @ValueSource(strings = {"utf-8", "euc-kr", "cp949"})
  void pooledTcpClient(String charsetName) throws Exception {
    final Charset charset = Charset.forName(charsetName);

    final TcpClient client = new PooledTcpClient(clientProperties, new NullHeaderStrategy(), true);

    final ObjectPool<Tuple> pool = getPool(client);
    assertEquals(poolConfig.getMinIdle(), pool.getNumIdle());

    final String message = "안녕하세요?";
    final byte[] response = client.send(message.getBytes(charset));

    assertEquals(message, new String(response, charset));
    assertEquals(poolConfig.getMinIdle(), pool.getNumIdle());
    assertEquals(0, pool.getNumActive());
    log.info("response: {}", new String(response, charset));
  }

  @Test
    // 동시성 문제가 있지만, 완전 못쓸 수준을 아니라 판단함
    // spring-data-redis 등도 apache.commons.pool2를 사용함
  void pooledTcpClient_underMultiThreads() {
    final Charset charset = DEFAULT_CHARSET;

    final TcpClient client = new PooledTcpClient(clientProperties, new NullHeaderStrategy(), true);
    final ObjectPool<Tuple> pool = getPool(client);
    final int noOfTests = poolConfig.getMinIdle();
    final Executor executor = Executors.newFixedThreadPool(noOfTests);
    final String message = "안녕하세요?";

    final List<CompletableFuture<byte[]>> futures = new ArrayList<>();
    for (int i = 0; i < noOfTests; i++) {
      final CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
        try {
          log.info("Sending message in a thread, pool state: numIdle={}, numActive={}",
              pool.getNumIdle(),
              pool.getNumActive());
          return client.send(message.getBytes(charset));
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
            log.info("response: {}", new String(f.get(), charset));
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
  @BeforeAll
  void setUp() {
    new Thread(() -> {
      try {
        server.start();
      } catch (Exception ignored) {
      }
    }).start();

    TimeUnit.SECONDS.sleep(3);
  }

  @SneakyThrows
  @AfterAll
  void shutDown() {
    TimeUnit.SECONDS.sleep(3);
    server.stop();
  }
}
