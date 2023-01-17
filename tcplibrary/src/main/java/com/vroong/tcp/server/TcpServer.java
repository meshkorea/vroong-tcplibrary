package com.vroong.tcp.server;

import java.io.InputStream;
import java.io.OutputStream;

public interface TcpServer {

  void start() throws Exception;

  void stop() throws Exception;

  void receive(InputStream reader, OutputStream writer) throws Exception;
}
