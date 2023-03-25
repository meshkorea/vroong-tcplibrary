package com.vroong.tcp.server;

import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public abstract class AbstractTcpServer implements TcpServer {

  @Setter
  private boolean keepConnection;

  private final int port;
  private final ExecutorService executor;
  private final HeaderStrategy strategy;
  private final ServerSocketFactory serverSocketFactory;
  private final boolean needClientAuth;

  private ServerSocket serverSocket;
  private final Map<Socket, Socket> socketHolder = new ConcurrentHashMap<>();

  public AbstractTcpServer(TcpServerProperties properties, HeaderStrategy strategy) {
    this(properties, strategy, false, false);
  }

  /**
   * Constructs a TcpServer.
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

  public abstract String receive(String received);

  public void start() throws Exception {
    this.serverSocket = serverSocketFactory.createServerSocket(port);
    if (serverSocket instanceof SSLServerSocket) {
      ((SSLServerSocket)serverSocket).setNeedClientAuth(needClientAuth);
    }

    log.info("Tcp server is listening at port {}", port);

    while (true) {
      Socket acceptedScoket = null;
      try {
        acceptedScoket = serverSocket.accept(); // 여기서 블록킹하고 있다가, 클라이언트가 접속하면 해제됨
      } catch (SocketException ignored) {
        // When closing the ServerSocket, the blocking thread will throw a SocketException.
        // https://docs.oracle.com/javase/7/docs/api/java/net/ServerSocket.html#close()
        return;
      }

      final Socket socket = acceptedScoket;
      socketHolder.putIfAbsent(socket, socket);
      if (log.isDebugEnabled()) {
        log.debug("A connection established with {}", socket.getRemoteSocketAddress());
      }

      CompletableFuture.runAsync(() -> {
        try {
          final BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
          final BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());

          final String received = strategy.read(input);
          final String response = receive(received);
          strategy.write(output, response);

          if (log.isDebugEnabled()) {
            log.debug("receive={}, send={}", received, response);
          }
        } catch (SSLHandshakeException ignored) {
          // Note: The following exceptional situation happens when the server is shutting down:
          //   sun.security.ssl.SSLSocketImpl#readHandshakeRecord:1429 throws SSLHandshakeException
          //   Remote host terminated the handshake...
        } catch (IOException e) {
          log.warn("{}: {}", e.getMessage(), socket.getPort());
        } finally {
          if (!keepConnection) {
            try {
              socket.close();
              if (log.isDebugEnabled()) {
                log.debug("A connection with {} is closed", socket.getRemoteSocketAddress());
              }
            } catch (IOException e) {
              log.error(String.format("Connection to port %s was not closed", socket.getPort()));
            }
          }

          if (socketHolder.containsKey(socket)) {
            socketHolder.remove(socket);
          }
        }
      }, executor);
    }
  }

  public void stop() throws Exception {
    if (socketHolder != null) {
      socketHolder.entrySet().forEach(entry -> {
        final Socket socket = entry.getValue();
        try {
          socket.close();
        } catch (IOException e) {
          log.error(String.format("Connection to port %s was not closed", socket.getPort()));
        }
      });
    }

    serverSocket.close();

    executor.awaitTermination(5, TimeUnit.SECONDS);
    executor.shutdown();
  }
}
