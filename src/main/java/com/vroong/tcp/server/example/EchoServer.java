package com.vroong.tcp.server.example;

import com.vroong.tcp.config.TcpServerProperties;
import com.vroong.tcp.server.AbstractTcpServer;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServer extends AbstractTcpServer {

  public EchoServer(TcpServerProperties properties) {
    super(properties);
  }

  public static void main(String[] args) throws Exception {
    final Properties properties = new Properties();

    try (final InputStream inputStream = AbstractTcpServer.class.getResourceAsStream("src/main/resources/application.yml")) {
      properties.load(inputStream);
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }
    properties.entrySet().forEach(System.out::println);

//    new EchoServer(new TcpServerProperties()).start();
  }

  @SneakyThrows
  @Override
  public void receive(InputStream reader, OutputStream writer) {
    strategy.write(writer, strategy.read(reader));
  }
}
