package com.vroong.tcp;

import java.io.ByteArrayOutputStream;
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
    int receivedSize = 0;

    final ByteArrayOutputStream received = new ByteArrayOutputStream();
    while (EOF != (receivedSize = reader.read(buffer))) {
      received.write(buffer, 0, receivedSize);

      if (containsNewLine(buffer)) {
        return Arrays.copyOf(received.toByteArray(), received.size() - 1); // "\n" size 제외
      }
    }

    return received.toByteArray();
  }
}
