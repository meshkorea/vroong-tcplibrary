package com.vroong.tcp.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GlobalConstants {

  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  public static final String LINE_FEED = System.lineSeparator();
  public static final String DIR_SEPARATOR = System.getProperty("file.separator");
  public static final String PROJECT_ROOT = System.getProperty("user.home") + DIR_SEPARATOR;

  private GlobalConstants() {
  }
}
