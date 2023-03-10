package com.vroong.tcp.client;

import static com.vroong.tcp.config.VroongTcpConstants.DEFAULT_CHARSET;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.vroong.tcp.TestHelper;
import com.vroong.tcp.client.PooledTcpClient.Tuple;
import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.config.TcpClientProperties.Pool;
import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import com.vroong.tcp.message.strategy.LengthAwareHeaderStrategy;
import com.vroong.tcp.server.example.LengthAwareEchoServer;
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
class LengthAwareTcpClientTest {

  TestHelper libraryTest = new TestHelper();

  TcpServerProperties serverProperties = libraryTest.getServerProperties();
  TcpClientProperties clientProperties = libraryTest.getClientProperties();
  Pool poolConfig = clientProperties.getPool();

  LengthAwareEchoServer server = new LengthAwareEchoServer(serverProperties);

  HeaderStrategy strategy = new LengthAwareHeaderStrategy('0', 4, DEFAULT_CHARSET);

  @ParameterizedTest
  @ValueSource(strings = {"utf-8", "euc-kr", "cp949"})
  void disposableTcpClient(String charsetName) throws Exception {
    final Charset charset = Charset.forName(charsetName);

    final TcpClient client = new DisposableTcpClient(clientProperties, strategy, false);

    final String message = "????????????????";
    final byte[] response = client.send(message.getBytes(charset));

    assertEquals(message, new String(response, charset));
  }

  @ParameterizedTest
  @ValueSource(strings = {"utf-8", "euc-kr", "cp949"})
  void pooledTcpClient(String charsetName) throws Exception {
    final Charset charset = Charset.forName(charsetName);

    final TcpClient client = new PooledTcpClient(clientProperties, strategy, false);

    final ObjectPool<Tuple> pool = getPool(client);
    assertEquals(poolConfig.getMinIdle(), pool.getNumIdle());

    final String message = "????????????????";
    final byte[] response = client.send(message.getBytes(charset));

    assertEquals(message, new String(response, charset));
    assertEquals(poolConfig.getMinIdle(), pool.getNumIdle());
    assertEquals(0, pool.getNumActive());
    log.info("response: {}", new String(response, charset));
  }

  @Test
    // ????????? ????????? ?????????, ?????? ?????? ????????? ????????? ?????????
    // spring-data-redis ?????? apache.commons.pool2??? ?????????
  void pooledTcpClient_underMultiThreads() {
    final Charset charset = DEFAULT_CHARSET;

    final TcpClient client = new PooledTcpClient(clientProperties, strategy, false);
    final ObjectPool<Tuple> pool = getPool(client);
    final int noOfTests = poolConfig.getMinIdle();
    final Executor executor = Executors.newFixedThreadPool(noOfTests);
    final String message = "????????????????";

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
    // private method??? ???????????? ?????? Reflection ??????
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
