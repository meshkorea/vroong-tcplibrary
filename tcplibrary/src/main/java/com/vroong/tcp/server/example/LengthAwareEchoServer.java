package com.vroong.tcp.server.example;

import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import com.vroong.tcp.message.strategy.NoOpHeaderStrategy;
import com.vroong.tcp.server.AbstractTcpServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.vroong.tcp.config.VroongTcpConstants.DEFAULT_CHARSET;

@Slf4j
public class LengthAwareEchoServer extends AbstractTcpServer {

  public LengthAwareEchoServer(TcpServerProperties properties, HeaderStrategy headerStrategy) {
    super(properties, headerStrategy, false, false);
  }

  public static void main(String[] args) throws Exception {
    new LengthAwareEchoServer(new TcpServerProperties(), new NoOpHeaderStrategy(DEFAULT_CHARSET)).start();
  }

  @SneakyThrows
  @Override
  public String receive(String received) {
    return received;
  }
}
