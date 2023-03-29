package com.vroong.tcp.message.strategy;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Tcp header handling strategy.
 * We assume that the message has a header that contains length of the message including the header itself.
 *
 * e.g. when lpadChar='0', headerLength=4, the full message will be
 *      0015hello world
 *      which means the full message length is 15 bytes including the header itself.
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
   * Charset to use when read and write message
   */
  @Setter
  private Charset charset;

  public LengthAwareHeaderStrategy(char lpadStr, int headerLength, Charset charset) {
    this.lpadChar = lpadStr;
    this.headerLength = headerLength;
    this.charset = charset;
  }

  @Override
  public String read(InputStream input) throws IOException {
    final byte[] header = new byte[headerLength];
    input.read(header);

    int bodyLength = 0;
    try {
      bodyLength = Integer.parseInt(new String(header, charset)) - headerLength;
    } catch (NumberFormatException e) {
      log.error("Failed to parse body length ", e);
    }

    final byte[] body = new byte[bodyLength];
    input.read(body);

    return new String(body, charset);
  }

  @Override
  public void write(OutputStream output, String body) throws IOException {
    final String header = leftPad(body, headerLength);

    output.write(header.getBytes(charset));
    output.write(body.getBytes(charset));
    output.flush();
  }

  private String leftPad(String body, int headerLength) {
    String numString = String.valueOf(body.getBytes(charset).length + headerLength);
    final int padLength = headerLength - numString.length();
    for (int i = 0; i < padLength; i++) {
      numString = lpadChar + numString;
    }

    return numString;
  }
}
