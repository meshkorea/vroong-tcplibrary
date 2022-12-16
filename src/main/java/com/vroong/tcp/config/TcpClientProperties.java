package com.vroong.tcp.config;

import static com.vroong.tcp.utils.PropertyUtils.getClientPropertiesValue;

import java.nio.charset.Charset;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TcpClientProperties {

  String host = "localhost";
  int port = 65535;
  int connectionTimeout = 1_000; // millis
  int readTimeout = 5_000; // millis
  @Getter(AccessLevel.NONE)
  String charset = "utf-8";
  Pool pool = new Pool();

  public Charset getCharset() {
    return Charset.forName(charset);
  }

  public TcpClientProperties() {
    Map<String, Object> propertiesMap = getClientPropertiesValue();
    if (propertiesMap != null) {
      this.host = (String) propertiesMap.get("host");
      this.port = (Integer) propertiesMap.get("port");
      this.connectionTimeout = (Integer) propertiesMap.get("connectionTimeout");
      this.readTimeout = (Integer) propertiesMap.get("readTimeout");
      this.charset = (String) propertiesMap.get("charset");
      this.pool = (Pool) propertiesMap.get("pool");
    }
  }

  @Getter
  @Setter
  public static class Pool {

    int minIdle = 10;
    int maxIdle = 10;
    int maxTotal = 100;

    public Pool() {
    }

    public Pool(int minIdle, int maxIdle, int maxTotal) {
      this.minIdle = minIdle;
      this.maxIdle = maxIdle;
      this.maxTotal = maxTotal;
    }
  }
}
