package com.vroong.tcp.config;

import static com.vroong.tcp.utils.PropertyUtils.getServerPropertiesValue;

import java.nio.charset.Charset;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@ToString
@Data
public class TcpServerProperties {

  int port = 65_535;
  int maxConnection = 100;
  @Getter(AccessLevel.NONE)
  String charset = "utf-8";

  public Charset getCharset() {
    return Charset.forName(charset);
  }

  public TcpServerProperties() {
    Map<String, Object> propertiesMap = getServerPropertiesValue();
    if (propertiesMap != null) {
      this.port = (Integer) propertiesMap.get("port");
      this.maxConnection = (Integer) propertiesMap.get("maxConnection");
      this.charset = (String) propertiesMap.get("charset");
    }
  }
}
