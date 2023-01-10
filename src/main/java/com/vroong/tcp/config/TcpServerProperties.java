package com.vroong.tcp.config;

import lombok.Data;

@Data
public class TcpServerProperties {

  int port = 65_535;
  int maxConnection = 100;
//  String keyStore = PROJECT_ROOT + "Project/work/vroong-tcplibrary/src/main/resources/keystore-for-server.jks";
//  String keyStorePassword = "secret";
//  String trustStore = PROJECT_ROOT + "Project/work/vroong-tcplibrary/src/main/resources/truststore-for-server.jks";
//  String trustStorePassword = "secret";
  String keyStore;
  String keyStorePassword;
  String trustStore;
  String trustStorePassword;
}
