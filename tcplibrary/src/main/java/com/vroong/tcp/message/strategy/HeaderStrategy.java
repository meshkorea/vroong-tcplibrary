package com.vroong.tcp.message.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface HeaderStrategy {

  /**
   * Read message from InputStream
   *
   * @param reader
   * @return byte array which was read
   * @throws IOException
   */
  byte[] read(InputStream reader) throws IOException;

  /**
   * Write message to OutputStream
   *
   * @param writer
   * @param body byte array to write
   * @throws IOException
   */
  void write(OutputStream writer, byte[] body) throws IOException;
}
