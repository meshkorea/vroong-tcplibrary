package com.vroong.tcp.config;

import lombok.Data;

@Data
public class TcpServerProperties {

  int port = 65_535;
  int maxConnection = 100;
  String keyStore = GlobalConstants.PROJECT_ROOT + "src/main/resources/keystore-for-server.jks";
  String keyStorePassword = "secret";
  String trustStore = GlobalConstants.PROJECT_ROOT + "src/main/resources/truststore-for-client.jks";
  String trustStorePassword = "secret";
//  String keyStore;
//  String keyStorePassword;
//  String trustStore;
//  String trustStorePassword;
}
