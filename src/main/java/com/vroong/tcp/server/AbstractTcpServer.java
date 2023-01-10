package com.vroong.tcp.server;

import com.vroong.tcp.config.TcpServerProperties;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTcpServer {

  protected final int port;
  protected final ExecutorService executor;

  protected final AtomicReference<ServerSocket> serverSocketHolder = new AtomicReference<>();
  protected final AtomicReference<List<Socket>> socketHolder = new AtomicReference<>(new ArrayList<>());

  private final ServerSocketFactory serverSocketFactory;
  private final boolean needClientAuth;

  public AbstractTcpServer(TcpServerProperties properties) {
    this(properties, false, false);
  }

  public AbstractTcpServer(TcpServerProperties properties, boolean useTLS, boolean needClientAuth) {
    this.port = properties.getPort();
    this.needClientAuth = needClientAuth;
    this.executor = Executors.newFixedThreadPool(properties.getMaxConnection());
    if (useTLS) {
      System.setProperty("javax.net.ssl.keyStore", properties.getKeyStore());
      System.setProperty("javax.net.ssl.keyStorePassword", properties.getKeyStorePassword());
      System.setProperty("javax.net.debug", "all");

      System.setProperty("javax.net.ssl.trustStore", properties.getTrustStore());
      System.setProperty("javax.net.ssl.trustStorePassword", properties.getTrustStorePassword());
      System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    this.serverSocketFactory = useTLS
        ? SSLServerSocketFactory.getDefault()
        : ServerSocketFactory.getDefault();
  }

  public abstract void handleMessage(InputStream reader, OutputStream writer);

  @SneakyThrows
  public void start() {
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

          handleMessage(reader, writer);
        } catch (IOException e) {
          log.warn("{}: {}", e.getMessage(), socket.getPort());
        } finally {
          try {
            socket.close();
          } catch (IOException e) {
            log.error(String.format("Connection to port %s was not closed", socket.getPort()));
          }
        }
      }, executor);
    }
  }

  @SneakyThrows
  public void stop() {
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
