package com.vroong.tcp.client;

import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisposableTcpClient extends AbstractTcpClient {

  public DisposableTcpClient(TcpClientProperties properties) {
    super(properties);
  }

  public DisposableTcpClient(TcpClientProperties properties, HeaderStrategy strategy, boolean useTLS) {
    super(properties, strategy, useTLS);
  }

  @Override
  public byte[] send(byte[] body) throws Exception {
    final Socket socket = createSocket();
    final OutputStream writer = new BufferedOutputStream(socket.getOutputStream());
    final InputStream reader = new BufferedInputStream(socket.getInputStream());

    strategy.write(writer, body);
    final byte[] response = strategy.read(reader);

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
