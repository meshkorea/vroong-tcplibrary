package com.vroong.tcp.config;

import lombok.Data;

@Data
public class TcpServerProperties {

  int port = 65_535;
  int maxConnection = 100;
  String keyStore = VroongTcpConstants.PROJECT_ROOT + "src/main/resources/keystore-for-server.jks";
  String keyStorePassword = "secret";
  String trustStore = VroongTcpConstants.PROJECT_ROOT + "src/main/resources/truststore-for-server.jks";
  String trustStorePassword = "secret";
}
