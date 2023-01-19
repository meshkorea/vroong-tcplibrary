package com.vroong.tcp.server.example;

import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.server.AbstractTcpServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServer extends AbstractTcpServer {

  public EchoServer(TcpServerProperties properties) {
    super(properties);
  }

  public static void main(String[] args) throws Exception {
    new EchoServer(new TcpServerProperties()).start();
  }

  @SneakyThrows
  @Override
  public byte[] receive(byte[] received) {
    return received;
  }
}
