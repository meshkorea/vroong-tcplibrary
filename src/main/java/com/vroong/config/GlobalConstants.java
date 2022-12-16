package com.vroong.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

public class GlobalConstants {

  public static final String PIPE_SEPARATOR = "|";
  public static final String REGEX_SEPARATOR = "\\" + PIPE_SEPARATOR;
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  public static final String LINE_FEED = System.lineSeparator();
  //todo
  //외부화 방법 연구; property vs method parameter;
  public static final int NO_OF_PACKET_FIELDS = 5;
  public static final int NO_OF_SUBPACKET_FIELDS = 4;
  public static final String ROOT_PACKET_NAME = "root";

  public static String[] byteToStringArray(byte[] tcpMessage) {
    if (tcpMessage.length == 0) {
      return IntStream.range(0, NO_OF_SUBPACKET_FIELDS)
          .mapToObj(i -> "")
          .toArray(String[]::new);
    }

    return new String(tcpMessage, DEFAULT_CHARSET)
        .split(REGEX_SEPARATOR);
  }

  public static String getSubPacketName(String subPacketPrefix, int subPacketIndex) {
    return subPacketPrefix + subPacketIndex;
  }

  public static int getNoOfSubPacket(String[] parts) {
    return (parts.length - NO_OF_PACKET_FIELDS) / NO_OF_SUBPACKET_FIELDS;
  }

  private GlobalConstants() {
  }
}
