package com.vroong.tcp;

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
}
