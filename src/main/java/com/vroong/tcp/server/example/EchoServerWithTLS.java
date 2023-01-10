package com.vroong.tcp.server.example;

import com.vroong.tcp.TcpUtils;
import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.server.AbstractTcpServer;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServerWithTLS extends AbstractTcpServer {

  public EchoServerWithTLS(TcpServerProperties properties) {
    super(properties, true, true);
  }

  public static void main(String[] args) {
    new EchoServerWithTLS(new TcpServerProperties()).start();
  }

  @SneakyThrows
  @Override
  public void handleMessage(InputStream reader, OutputStream writer) {
    final byte[] buffer = TcpUtils.readLine(reader);

    writer.write(buffer);
    writer.flush();
  }
}
