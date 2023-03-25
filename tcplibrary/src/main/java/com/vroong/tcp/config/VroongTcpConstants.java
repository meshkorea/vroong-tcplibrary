package com.vroong.tcp.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class VroongTcpConstants {

  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  public static final String NEW_LINE = System.lineSeparator();
  public static final String DIR_SEPARATOR = System.getProperty("file.separator");
  public static final String PROJECT_ROOT = System.getProperty("user.dir") + DIR_SEPARATOR;

  private VroongTcpConstants() {
  }
}
