package com.vroong.tcp.client;

import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import lombok.extern.slf4j.Slf4j;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public abstract class AbstractTcpClient implements TcpClient {

  protected final HeaderStrategy strategy;

  private final String host;
  private final int port;
  private int connectionTimeout;
  private int readTimeout;

  private final SocketFactory socketFactory;

  public AbstractTcpClient(TcpClientProperties properties, HeaderStrategy strategy) {
    this(properties, strategy, false);
  }

  /**
   * Constructs a TcpClient.
   *
   * @param properties
   * @param strategy
   * @param useTLS true if communication requires TLS, otherwise false
   */
  public AbstractTcpClient(TcpClientProperties properties, HeaderStrategy strategy, boolean useTLS) {
    this.strategy = strategy;
    this.host = properties.getHost();
    this.port = properties.getPort();
    this.connectionTimeout = properties.getConnectionTimeout();
    this.readTimeout = properties.getReadTimeout();

    if (useTLS) {
      System.setProperty("javax.net.ssl.keyStore", properties.getKeyStore());
      System.setProperty("javax.net.ssl.keyStorePassword", properties.getKeyStorePassword());

      System.setProperty("javax.net.ssl.trustStore", properties.getTrustStore());
      System.setProperty("javax.net.ssl.trustStorePassword", properties.getTrustStorePassword());
      System.setProperty("javax.net.ssl.trustStoreType", "JKS");

      if (log.isDebugEnabled()) {
        System.setProperty("javax.net.debug", "all");
        log.debug("javax.net.ssl.keyStore={}, javax.net.ssl.keyStorePassword={}, javax.net.ssl.trustStore={}, javax.net.ssl.trustStorePassword={}, javax.net.ssl.trustStoreType={}",
            properties.getKeyStore(), properties.getKeyStorePassword(), properties.getTrustStore(), properties.getTrustStorePassword(), "JKS");
      }
    }

    this.socketFactory = useTLS ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
  }

  @Override
  public abstract String send(String message) throws Exception;

  protected Socket createSocket() throws Exception {
    final Socket socket = socketFactory.createSocket();
    // Java 소켓 옵션 설정하기 @see https://cbts.tistory.com/125
    socket.setSoTimeout(readTimeout); // read() 메서드가 블록킹할 시간
    socket.setSoLinger(true, 0);      // 소켓이 닫히면 전송되지 않은 소켓은 버린다
    socket.setTcpNoDelay(true);       // 패킷의 크기에 상관없이 가능한 한 빨리 패킷을 전송한다
    socket.setKeepAlive(true);        // 유휴 연결에 패킷을 보내 소켓을 계속 유지한다
    socket.setReuseAddress(true);     // 소켓을 닫을 때, 로컬 포트를 즉시 닫지 않고, 다른 소켓이 포트를 쓸 수 있도록 한다

    try {
      socket.connect(new InetSocketAddress(host, port), connectionTimeout);
      if (socket.isConnected()) {
        if (log.isDebugEnabled()) {
          log.debug("Connected to {}, srcPort={}, connectionTimeout={}, readTimeout={}", socket.getRemoteSocketAddress(),
              socket.getLocalPort(), connectionTimeout, socket.getSoTimeout());
        }
      } else {
        throw new SocketConnectionFailedException(
            String.format("Connection to %s failed", socket.getRemoteSocketAddress()));
      }
    } catch (IOException e) {
      log.error(String.format("Connection to %s failed", socket.getRemoteSocketAddress()), e);
      throw new SocketConnectionFailedException(
          String.format("Connection to %s failed: %s", socket.getRemoteSocketAddress(),
              e.getMessage()), e);
    }

    return socket;
  }
}
