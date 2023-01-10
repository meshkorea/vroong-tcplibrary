package com.vroong.tcp.client;

import com.vroong.tcp.config.TcpClientProperties;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTcpClient implements TcpClient {

  @Getter
  private String host;

  @Getter
  private int port;

  @Setter
  protected int connectionTimeout;

  @Setter
  protected int readTimeout;

  private final SocketFactory socketFactory;

  public AbstractTcpClient(TcpClientProperties properties, boolean useTLS) {
    this.host = properties.getHost();
    this.port = properties.getPort();
    this.connectionTimeout = properties.getConnectionTimeout();
    this.readTimeout = properties.getReadTimeout();
    if (useTLS) {
      System.setProperty("javax.net.ssl.trustStore", properties.getTrustStore());
      System.setProperty("javax.net.ssl.trustStorePassword", properties.getTrustStorePassword());
      System.setProperty("javax.net.ssl.trustStoreType", "JKS");

      System.setProperty("javax.net.ssl.keyStore", properties.getKeyStore());
      System.setProperty("javax.net.ssl.keyStorePassword", properties.getKeyStorePassword());
//      System.setProperty("javax.net.debug", "all");
    }
    this.socketFactory = useTLS ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
  }

  @Override
  public abstract void write(byte[] message) throws Exception;

  @Override
  public abstract byte[] read() throws Exception;

  protected Socket createSocket()
      throws Exception {
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
          log.debug("Connected to {}, srcPort={}", socket.getRemoteSocketAddress(),
              socket.getLocalPort());
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
