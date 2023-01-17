package com.vroong.tcp.message.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;

/**
 * Tcp header handling strategy, which has a header that contains length of the message
 *
 * e.g. when lpadChar='0', headerLength=4, the full message will be
 *      0015hello world
 *      which means the full message length is 15 bytes including the header itself.
 *
 */
@Slf4j
public class LengthAwareHeaderStrategy implements HeaderStrategy {

  /**
   * Left pad char prefixed before headerLength e.g. '0'
   */
  private final char lpadChar;

  /**
   * Length of header e.g. 4
   */
  private final int headerLength;

  /**
   * Charset to use when read header
   */
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
