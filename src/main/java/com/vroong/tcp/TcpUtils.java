package com.vroong.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class TcpUtils {

  static final int EOF = -1;

  public static boolean containsNewLine(byte[] haystack) {
    byte[] needle = new byte[]{ 10 };
    for (int i = 0; i <= haystack.length - needle.length; i++) {
      int j = 0;
      while (j < needle.length && haystack[i + j] == needle[j]) {
        j++;
      }
      if (j == needle.length) {
        return true;
      }
    }
    return false;
  }

  public static byte[] readLine(InputStream reader) throws IOException {
    byte[] buffer = new byte[8192]; // InputStream DEFAULT_BUFFER_SIZE
    int totalSize = 0;
    int currentSize = 0;

    while (EOF != (currentSize = reader.read(buffer))) {
      totalSize += currentSize;
      if (containsNewLine(buffer)) {
        totalSize -= 1; // "\n" size 제외
        break;
      }
    }

    return Arrays.copyOfRange(buffer, 0, totalSize); // byte array padding 제거
  }
}
