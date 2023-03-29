package com.vroong.vroongtcp.autoconfigure;

import com.vroong.tcp.client.DisposableTcpClient;
import com.vroong.tcp.client.TcpClient;
import com.vroong.tcp.config.VroongTcpConstants;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import com.vroong.tcp.message.strategy.NoOpHeaderStrategy;
import java.io.IOException;
import java.net.Socket;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(value = {TcpClientProperties.class, TcpServerProperties.class})
public class VroongTcpAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  HeaderStrategy headerStrategy() {
    return new NoOpHeaderStrategy(VroongTcpConstants.DEFAULT_CHARSET);
  }

  @Bean
  @ConditionalOnMissingBean
  TcpClient tcpClient(TcpClientProperties tcpClientProperties, HeaderStrategy headerStrategy) {
    return new DisposableTcpClient(tcpClientProperties, headerStrategy, false);
  }

  @Bean("tcp")
  @ConditionalOnProperty(value = "tcp.server.health.enabled", havingValue = "true")
  @ConditionalOnClass(HealthIndicator.class)
  HealthIndicator health(TcpServerProperties serverProperties) {
    final int port = serverProperties.getPort();

    return new AbstractHealthIndicator() {
      @Override
      protected void doHealthCheck(Builder builder) throws Exception {
        try(Socket socket = new Socket("localhost", port)) {
          builder.status(Status.UP).withDetail("tcp", "Tcp server is listening at port " + port);
        } catch (IOException e) {
          final String value = String.format("Failed to connect to port %d: %s", port, e.getMessage());
          builder.status(Status.DOWN).withDetail("tcp", value);
          throw e;
        }
      }
    };
  }
}
