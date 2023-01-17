package com.vroong.vroongtcp.demo;

import com.vroong.tcp.client.TcpClient;
import com.vroong.tcp.server.example.EchoServer;
import com.vroong.vroongtcp.autoconfigure.TcpServerProperties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @Component
  static class DemoRunner implements ApplicationRunner {

    @Autowired
    TcpClient tcpClient;

    @Autowired
    TcpServerProperties tcpServerProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
      final Future<EchoServer> future = CompletableFuture.supplyAsync(
          () -> {
            final EchoServer echoServer = new EchoServer(tcpServerProperties);
            try {
              echoServer.start();
            } catch (Exception ignored) {
            }

            return echoServer;
          });

      final byte[] res = tcpClient.send("hello world".getBytes());
      System.out.println(new String(res));

      future.get().stop();
    }
  }
}
