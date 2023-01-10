package com.vroong.tcp.client;

import com.vroong.tcp.TcpUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisposableTcpClient extends AbstractTcpClient {

  private final String host;
  private final int port;

  private Socket socket;
  private OutputStream writer;
  private InputStream reader;

  public DisposableTcpClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public void write(byte[] message) throws Exception {
    socket = createSocket(host, port, connectionTimeout, readTimeout);
    writer = new BufferedOutputStream(socket.getOutputStream());
    writer.write(message);
    writer.flush();
  }

  @Override
  public byte[] read() throws Exception {
    reader = new BufferedInputStream(socket.getInputStream());
    final byte[] rawMessage = TcpUtils.readLine(reader);

    clearResources();

    return rawMessage;
  }

  private void clearResources() throws Exception {
    if (socket != null) {
      socket.shutdownOutput();
      socket.shutdownInput();
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
