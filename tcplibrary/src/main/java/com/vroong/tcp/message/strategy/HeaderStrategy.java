package com.vroong.tcp.message.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface HeaderStrategy {

  /**
   * Reads a message from the given InputStream
   *
   * @param input
   * @return String representation of message received via InputStream
   * @throws IOException
   */
  String read(InputStream input) throws IOException;

  /**
   * Writes a message to the given OutputStream
   *
   * @param output
   * @param body Stringified message to send via OutputStream
   * @throws IOException
   */
  void write(OutputStream output, String body) throws IOException;
}
