package com.vroong.tcp.message.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LengthAwareHeaderStrategy implements HeaderStrategy {

  private final char lpadChar;
  private final int headerLength;
  private final Charset charset;

  public LengthAwareHeaderStrategy(char lpadStr, int headerLength, Charset charset) {
    this.lpadChar = lpadStr;
    this.headerLength = headerLength;
    this.charset = charset;
  }

  @Override
  public byte[] read(InputStream reader) throws IOException {
    final byte[] header = new byte[headerLength];
    reader.read(header);

    int bodyLength = 0;
    try {
      bodyLength = Integer.parseInt(new String(header, charset)) - headerLength;
    } catch (NumberFormatException e) {
      log.error("Failed to parse body length ", e);
    }

    final byte[] body = new byte[bodyLength];
    reader.read(body);

    return body;
  }

  @Override
  public void write(OutputStream writer, byte[] body) throws IOException {
    final byte[] header = leftPad(body, headerLength);

    writer.write(header);
    writer.write(body);
    writer.flush();
  }

  private byte[] leftPad(byte[] body, int headerLength) {
    String numString = String.valueOf(body.length + headerLength);
    final int padLength = headerLength - numString.length();
    for (int i = 0; i < padLength; i++) {
      numString = lpadChar + numString;
    }

    return numString.getBytes();
  }
}
