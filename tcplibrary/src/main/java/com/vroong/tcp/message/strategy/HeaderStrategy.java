package com.vroong.tcp.message.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface HeaderStrategy {

  byte[] read(InputStream reader) throws IOException;

  void write(OutputStream writer, byte[] body) throws IOException;
}
