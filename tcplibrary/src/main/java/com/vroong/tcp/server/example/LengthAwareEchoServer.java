package com.vroong.tcp.server.example;

import static com.vroong.tcp.config.VroongTcpConstants.DEFAULT_CHARSET;

import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.message.strategy.LengthAwareHeaderStrategy;
import com.vroong.tcp.server.AbstractTcpServer;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LengthAwareEchoServer extends AbstractTcpServer {

  public LengthAwareEchoServer(TcpServerProperties properties) {
    super(properties,
        new LengthAwareHeaderStrategy('0', 4, DEFAULT_CHARSET),
        false,
        false);
  }

  public static void main(String[] args) throws Exception {
    new LengthAwareEchoServer(new TcpServerProperties()).start();
  }

  @SneakyThrows
  @Override
  public void receive(InputStream reader, OutputStream writer) {
    strategy.write(writer, strategy.read(reader));
  }
}
