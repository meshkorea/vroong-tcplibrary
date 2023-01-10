package com.vroong.tcp.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TcpClientProperties {

  String host = "localhost";
  int port = 65_535;
  int connectionTimeout = 1_000; // millis
  int readTimeout = 5_000; // millis
//  String keyStore = PROJECT_ROOT + "Project/work/vroong-tcplibrary/src/main/resources/keystore-for-client.jks";
//  String keyStorePassword = "secret";
//  String trustStore = PROJECT_ROOT + "Project/work/vroong-tcplibrary/src/main/resources/truststore-for-client.jks";
//  String trustStorePassword = "secret";
  String keyStore;
  String keyStorePassword;
  String trustStore;
  String trustStorePassword;
  Pool pool = new Pool();

  @Getter
  @Setter
  public static class Pool {

    final int minIdle = 10;
    final int maxIdle = 10;
    final int maxTotal = 100;
  }
}
