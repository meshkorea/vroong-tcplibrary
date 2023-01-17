package com.vroong.vroongtcp.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tcp.server")
public class TcpServerProperties extends com.vroong.tcp.config.TcpServerProperties {

}
