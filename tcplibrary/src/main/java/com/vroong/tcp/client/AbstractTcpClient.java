package com.vroong.tcp.client;

import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.config.VroongTcpConstants;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import com.vroong.tcp.message.strategy.NullHeaderStrategy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTcpClient implements TcpClient {

  @Setter
  protected Charset charset = VroongTcpConstants.DEFAULT_CHARSET;

  protected final HeaderStrategy strategy;

  @Getter
  private final String host;

  @Getter
  private final int port;

  @Setter
  private int connectionTimeout;

  @Setter
  private int readTimeout;

  private final SocketFactory socketFactory;

  public AbstractTcpClient(TcpClientProperties properties) {
    this(properties, new NullHeaderStrategy(), false);
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
  public abstract byte[] send(byte[] message) throws Exception;

  protected Socket createSocket() throws Exception {
    final Socket socket = socketFactory.createSocket();
    // Java ?????? ?????? ???????????? @see https://cbts.tistory.com/125
    socket.setSoTimeout(readTimeout); // read() ???????????? ???????????? ??????
    socket.setSoLinger(true, 0);      // ????????? ????????? ???????????? ?????? ????????? ?????????
    socket.setTcpNoDelay(true);       // ????????? ????????? ???????????? ????????? ??? ?????? ????????? ????????????
    socket.setKeepAlive(true);        // ?????? ????????? ????????? ?????? ????????? ?????? ????????????
    socket.setReuseAddress(true);     // ????????? ?????? ???, ?????? ????????? ?????? ?????? ??????, ?????? ????????? ????????? ??? ??? ????????? ??????

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
