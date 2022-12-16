package com.vroong.tcp.server;

import com.vroong.tcp.config.TcpServerProperties;
import java.io.BufferedReader;
import java.io.PrintWriter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServer extends AbstractTcpServer {

  public EchoServer(TcpServerProperties properties) {
    super(properties);
  }

  @SneakyThrows
  @Override
  public void handleMessage(BufferedReader reader, PrintWriter writer) {
    String message;
    while ((message = reader.readLine()) != null) {
      writer.println(message);
    }
  }
}
