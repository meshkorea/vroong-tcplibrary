package com.vroong.tcp.message.strategy;

import lombok.Setter;

import java.io.*;
import java.nio.charset.Charset;

import static com.vroong.tcp.config.VroongTcpConstants.NEW_LINE;

/**
 * Tcp header handling strategy.
 * We assume that there is a new line character in the message as an EOF marker.
 *
 * e.g. hello world\n
 */
public class NoOpHeaderStrategy implements HeaderStrategy {

  @Setter
  private Charset charset;

  public NoOpHeaderStrategy(Charset charset) {
    this.charset = charset;
  }

  @Override
  public String read(InputStream input) throws IOException {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(input, charset));
    return reader.readLine();
  }

  @Override
  public void write(OutputStream output, String body) throws IOException {
    output = new BufferedOutputStream(output);
    output.write(body.getBytes(charset));
    output.write(NEW_LINE.getBytes(charset));
    output.flush();
  }
}
