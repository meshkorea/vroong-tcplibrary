package com.vroong.tcp.utils;

import com.vroong.tcp.config.TcpClientProperties.Pool;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class PropertyUtils {

  private static final String TCP_CONFIG_YAML_PATH =
      System.getProperty("user.dir") + "/src/main/resources/" + "tcp_config.yml";

  public static Map<String, Object> getServerPropertiesValue() throws FileNotFoundException {
    Map<String, Object> propMap =
        new Yaml().load(new FileReader(TCP_CONFIG_YAML_PATH));
    Map<String, Object> parameterMap = new HashMap<>();
    Object tcp = propMap.get("tcp");

    if (tcp instanceof Map) {
      Map<String, Object> tcpMap = (Map<String, Object>) tcp;
      Object server = tcpMap.get("server");

      if (server instanceof Map) {
        Map<String, Object> serverMap = (Map<String, Object>) server;
        if (serverMap.get("port") != null) {
          parameterMap.put("port", serverMap.get("port"));
        }
        if (serverMap.get("max-connection") != null) {
          parameterMap.put("maxConnection", serverMap.get("max-connection"));
        }
        if (serverMap.get("charset") != null) {
          parameterMap.put("charset", serverMap.get("charset"));
        }
      }

      if (!parameterMap.isEmpty()) {
        return parameterMap;
      }
    }

    return null;
  }

  public static Map<String, Object> getClientPropertiesValue() throws FileNotFoundException {
    Map<String, Object> propMap = new Yaml().load(new FileReader(TCP_CONFIG_YAML_PATH));
    Map<String, Object> parameterMap = new HashMap<>();
    Object tcp = propMap.get("tcp");

    if (tcp instanceof Map) {
      Map<String, Object> tcpMap = (Map<String, Object>) tcp;
      Object client = tcpMap.get("client");

      if (client instanceof Map) {
        Map<String, Object> clientMap = (Map<String, Object>) client;
        if (clientMap.get("host") != null) {
          parameterMap.put("host", clientMap.get("host"));
        }
        if (clientMap.get("port") != null) {
          parameterMap.put("port", clientMap.get("port"));
        }
        if (clientMap.get("connection-timeout") != null) {
          parameterMap.put("connectionTimeout", clientMap.get("connection-timeout"));
        }
        if (clientMap.get("read-timeout") != null) {
          parameterMap.put("readTimeout", clientMap.get("connection-timeout"));
        }
        if (clientMap.get("charset") != null) {
          parameterMap.put("charset", clientMap.get("charset"));
        }
        if (clientMap.get("pool") != null) {
          Map<String, Integer> poolMap = (Map<String, Integer>) clientMap.get("pool");
          Pool pool = new Pool(
              poolMap.get("min-idle"),
              poolMap.get("max-idle"),
              poolMap.get("max-total")
          );
          parameterMap.put("pool", pool);

        }
      }

      if (!parameterMap.isEmpty()) {
        return parameterMap;
      }
    }

    return null;
  }
}
