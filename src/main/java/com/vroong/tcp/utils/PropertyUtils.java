package com.vroong.tcp.utils;

import com.vroong.tcp.config.TcpClientProperties.Pool;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

@Slf4j
public class PropertyUtils {

  private static final String TCP_CONFIG_YAML_PATH =
      System.getProperty("user.dir") + "/src/main/resources/" + "tcp_config.yml";

  public static Map<String, Object> getServerPropertiesValue() {
    Map<String, Object> propMap = getPropMap();
    if (propMap == null) {
      return null;
    }

    Object tcp = propMap.get("tcp");
    Map<String, Object> parameterMap = new HashMap<>();
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

  public static Map<String, Object> getClientPropertiesValue() {
    Map<String, Object> propMap = getPropMap();
    if (propMap == null) {
      return null;
    }

    Object tcp = propMap.get("tcp");
    Map<String, Object> parameterMap = new HashMap<>();
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

  private static Map<String, Object> getPropMap() {
    Map<String, Object> propMap = null;
    try {
      propMap = new Yaml().load(new FileReader(TCP_CONFIG_YAML_PATH));
    } catch (FileNotFoundException e) {
      log.error(e.getMessage());
    }

    if (propMap == null) {
      return null;
    }
    return propMap;
  }
}
