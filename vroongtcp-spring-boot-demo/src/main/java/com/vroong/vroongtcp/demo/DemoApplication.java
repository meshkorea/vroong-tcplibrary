package com.vroong.vroongtcp.demo;

import com.vroong.tcp.client.TcpClient;
import com.vroong.tcp.message.strategy.NoOpHeaderStrategy;
import com.vroong.tcp.server.TcpServer;
import com.vroong.tcp.server.example.EchoServer;
import com.vroong.vroongtcp.autoconfigure.TcpServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.vroong.tcp.config.VroongTcpConstants.DEFAULT_CHARSET;

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
      final CompletableFuture<TcpServer> future = CompletableFuture
          .supplyAsync(() -> {
            final EchoServer echoServer = new EchoServer(tcpServerProperties,
                new NoOpHeaderStrategy(DEFAULT_CHARSET));
            try {
              echoServer.start();
            } catch (Exception ignored) {
            }

            return echoServer;
          });

      // 다른 쓰레드에서 서버가 기동될 동안 대기한다
      Thread.sleep(3_000);

      final String res = tcpClient.send("hello world");
      System.out.println(res);
    }
  }
}
