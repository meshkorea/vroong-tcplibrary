package com.vroong.tcp.message.strategy;

import static com.vroong.tcp.TcpUtils.containsNewLine;
import static com.vroong.tcp.config.GlobalConstants.DEFAULT_BUFFER_SIZE;
import static com.vroong.tcp.config.GlobalConstants.EOF;
import static com.vroong.tcp.config.GlobalConstants.NEW_LINE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class NullHeaderStrategy implements HeaderStrategy {

  @Override
  public byte[] read(InputStream reader) throws IOException {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
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

  @Override
  public void write(OutputStream writer, byte[] body) throws IOException {
    writer.write(body);
    writer.write(NEW_LINE.getBytes());
    writer.flush();
  }
}
