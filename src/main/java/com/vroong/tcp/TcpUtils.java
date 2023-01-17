package com.vroong.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class TcpUtils {

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

  public static String inputStreamToString(InputStream in) {
    StringBuilder builder = new StringBuilder();
    try (Reader reader = new BufferedReader(new InputStreamReader(in))) {
      int c = 0;
      while ((c = reader.read()) != -1) {
        builder.append((char) c);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return builder.toString();
  }
}
