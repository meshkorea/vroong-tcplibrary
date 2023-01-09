package com.vroong.tcp.server;

import com.vroong.tcp.TcpUtils;
import com.vroong.tcp.config.TcpServerProperties;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServer extends AbstractTcpServer {

  public EchoServer(TcpServerProperties properties) {
    super(properties);
  }

  public static void main(String[] args) {
    new EchoServer(new TcpServerProperties()).start();
  }

  @SneakyThrows
  @Override
  public void handleMessage(InputStream reader, OutputStream writer) {
    final byte[] buffer = TcpUtils.readLine(reader);

    writer.write(buffer);
    writer.flush();
  }
}
