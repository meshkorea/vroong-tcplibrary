package com.vroong.vroongtcp.autoconfigure;

import com.vroong.tcp.client.DisposableTcpClient;
import com.vroong.tcp.client.TcpClient;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import com.vroong.tcp.message.strategy.NullHeaderStrategy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(value = {TcpClientProperties.class, TcpServerProperties.class})
public class VroongTcpAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  HeaderStrategy nullHeaderStrategy() {
    return new NullHeaderStrategy();
  }

  @Bean
  @ConditionalOnMissingBean
  TcpClient tcpClient(TcpClientProperties tcpClientProperties, HeaderStrategy headerStrategy) {
    return new DisposableTcpClient(tcpClientProperties, headerStrategy, false);
  }
}
