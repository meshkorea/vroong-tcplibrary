package com.vroong.tcp.server;

import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import com.vroong.tcp.message.strategy.NullHeaderStrategy;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTcpServer implements TcpServer {

  protected final int port;
  protected final ExecutorService executor;
  protected final HeaderStrategy strategy;

  protected final AtomicReference<ServerSocket> serverSocketHolder = new AtomicReference<>();
  protected final AtomicReference<List<Socket>> socketHolder = new AtomicReference<>(new ArrayList<>());

  private final ServerSocketFactory serverSocketFactory;

  private final boolean needClientAuth;

  public AbstractTcpServer(TcpServerProperties properties) {
    this(properties, new NullHeaderStrategy(), false, false);
  }

  /**
   * Construct a TcpServer.
   *
   * @param properties
   * @param strategy
   * @param useTLS true if communication requires TLS, otherwise false
   * @param needClientAuth true if the client certificate is required, otherwise false
   */
  public AbstractTcpServer(TcpServerProperties properties, HeaderStrategy strategy, boolean useTLS, boolean needClientAuth) {
    this.port = properties.getPort();
    this.executor = Executors.newFixedThreadPool(properties.getMaxConnection());
    this.strategy = strategy;

    if (useTLS) {
      System.setProperty("javax.net.ssl.keyStore", properties.getKeyStore());
      System.setProperty("javax.net.ssl.keyStorePassword", properties.getKeyStorePassword());

      System.setProperty("javax.net.ssl.trustStore", properties.getTrustStore());
      System.setProperty("javax.net.ssl.trustStorePassword", properties.getTrustStorePassword());
      System.setProperty("javax.net.ssl.trustStoreType", "JKS");

      if (log.isDebugEnabled()) {
        System.setProperty("javax.net.debug", "all");
      }
    }

    this.serverSocketFactory = useTLS
        ? SSLServerSocketFactory.getDefault()
        : ServerSocketFactory.getDefault();

    this.needClientAuth = needClientAuth;
  }

  public abstract void receive(InputStream reader, OutputStream writer);

  public void start() throws Exception {
    final ServerSocket serverSocket = serverSocketFactory.createServerSocket(port);
    if (serverSocket instanceof SSLServerSocket) {
      ((SSLServerSocket)serverSocket).setNeedClientAuth(needClientAuth);
    }

    this.serverSocketHolder.set(serverSocket);
    log.info("Tcp server is listening at port {}", port);

    while (true) {
      final Socket socket = serverSocket.accept(); // 여기서 블록킹하고 있다가, 클라이언트가 접속하면 해제됨
      socketHolder.get().add(socket);
      log.info("A connection established to port {}", socket.getPort());

      CompletableFuture.runAsync(() -> {
        try {
          final BufferedInputStream reader = new BufferedInputStream(socket.getInputStream());
          final BufferedOutputStream writer = new BufferedOutputStream(socket.getOutputStream());

          receive(reader, writer);
        } catch (IOException e) {
          log.warn("{}: {}", e.getMessage(), socket.getPort());
        } finally {
          // TODO: 클라이언트가 PooledTcpClient를 사용할 경우에 대한 처리 필요
          try {
            socket.close();
          } catch (IOException e) {
            log.error(String.format("Connection to port %s was not closed", socket.getPort()));
          }
        }
      }, executor);
    }
  }

  public void stop() throws Exception {
    if (socketHolder != null) {
      socketHolder.get().forEach(socket -> {
        try {
          socket.close();
        } catch (IOException e) {
          log.error(String.format("Connection to port %s was not closed", socket.getPort()));
        }
      });
    }

    if (serverSocketHolder != null && serverSocketHolder.get() != null) {
      serverSocketHolder.get().close();
    }

    executor.awaitTermination(5, TimeUnit.SECONDS);
    executor.shutdown();
  }
}
