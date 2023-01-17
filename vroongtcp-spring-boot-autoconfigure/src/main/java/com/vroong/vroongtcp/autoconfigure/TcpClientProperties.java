package com.vroong.vroongtcp.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tcp.client")
public class TcpClientProperties extends com.vroong.tcp.config.TcpClientProperties {

}
