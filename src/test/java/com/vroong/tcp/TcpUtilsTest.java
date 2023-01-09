package com.vroong.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class TcpUtilsTest {

  @ParameterizedTest
  @ArgumentsSource(ByteArrayProvider.class)
  void containsNewLine(byte[] vector, boolean expected) {
    assertEquals(expected, TcpUtils.containsNewLine(vector));
  }

  static class ByteArrayProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
          Arguments.of("안녕하세요".getBytes(), false),
          Arguments.of("안녕하세요".getBytes("utf-8"), false),
          Arguments.of("안녕하세요".getBytes("cp949"), false),
          Arguments.of("안녕하세요".getBytes("euc-kr"), false),
          Arguments.of("안녕하세요\n".getBytes(), true),
          Arguments.of("안녕하세요\n".getBytes("utf-8"), true),
          Arguments.of("안녕하세요\n".getBytes("cp949"), true),
          Arguments.of("안녕하세요\n".getBytes("euc-kr"), true),
          Arguments.of("안녕하세요\n 안녕하세요".getBytes(), true),
          Arguments.of("안녕하세요\n 안녕하세요".getBytes("utf-8"), true),
          Arguments.of("안녕하세요\n 안녕하세요".getBytes("cp949"), true),
          Arguments.of("안녕하세요\n 안녕하세요".getBytes("euc-kr"), true)
      );
    }
  }
}