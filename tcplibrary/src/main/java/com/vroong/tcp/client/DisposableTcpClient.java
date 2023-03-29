package com.vroong.tcp.client;

import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

/**
 * Constructs a TcpClient that closes the socket after use.
 */
@Slf4j
public class DisposableTcpClient extends AbstractTcpClient {

  public DisposableTcpClient(TcpClientProperties properties, HeaderStrategy strategy) {
    super(properties, strategy);
  }

  public DisposableTcpClient(TcpClientProperties properties, HeaderStrategy strategy, boolean useTLS) {
    super(properties, strategy, useTLS);
  }

  @Override
  public String send(String body) throws Exception {
    final Socket socket = createSocket();

    strategy.write(new BufferedOutputStream(socket.getOutputStream()), body);
    final String response = strategy.read(new BufferedInputStream(socket.getInputStream()));

    if (log.isDebugEnabled()) {
      log.debug("send={}, receive={}", body, response);
    }

    clearResources(socket);

    return response;
  }

  private void clearResources(Socket socket) throws Exception {
    if (socket != null) {
      socket.close();
    }

    if (log.isDebugEnabled()) {
      log.debug("Socket state after cleanup: socket={}, srcPort={}, connection={}, writer={}, reader={}",
          socket.getRemoteSocketAddress(), socket.getLocalPort(), socket.isConnected(), !socket.isOutputShutdown(),
          !socket.isInputShutdown());
    }

    socket = null;
  }
}
