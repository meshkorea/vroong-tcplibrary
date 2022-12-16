package com.vroong.config;

import com.vroong.tcp.client.DisposableTcpClient;
import com.vroong.tcp.client.TcpClient;
import com.vroong.tcp.config.TcpClientProperties;

public class TcpClientConfiguration {

  private final TcpClientProperties properties;

  public TcpClientConfiguration(TcpClientProperties properties) {
    this.properties = properties;
  }

  TcpClient defaultTcpClient() {
    return new DisposableTcpClient(properties.getHost(), properties.getPort(),
        properties.getCharset());
  }
}
