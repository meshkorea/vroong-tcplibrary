package com.vroong.tcp.server.example;

import com.vroong.tcp.TcpUtils;
import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.server.AbstractTcpServer;
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
