package com.vroong.tcp.server.example;

import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.NullHeaderStrategy;
import com.vroong.tcp.server.AbstractTcpServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServerWithTLS extends AbstractTcpServer {

  public EchoServerWithTLS(TcpServerProperties properties) {
    super(properties, new NullHeaderStrategy(), true, true);
  }

  public static void main(String[] args) throws Exception {
    new EchoServerWithTLS(new TcpServerProperties()).start();
  }

  @SneakyThrows
  @Override
  public byte[] receive(byte[] received) {
    return received;
  }
}
