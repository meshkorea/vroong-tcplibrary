package com.vroong.tcp.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class VroongTcpConstants {

  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  public static final String NEW_LINE = System.lineSeparator();
  public static final String DIR_SEPARATOR = System.getProperty("file.separator");
  public static final String PROJECT_ROOT = System.getProperty("user.dir") + DIR_SEPARATOR;
  public static final int DEFAULT_BUFFER_SIZE = 8192; // InputStream DEFAULT_BUFFER_SIZE
  public static final int EOF = -1;


  private VroongTcpConstants() {
  }
}
