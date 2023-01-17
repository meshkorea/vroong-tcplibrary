package com.vroong.tcp.server;

import java.io.InputStream;
import java.io.OutputStream;

public interface TcpServer {

  /**
   * Start a Tcp server, which blocks indefinitely waiting for a client connection.
   * Once a client is connected, a new worker thread will be allocated.
   *
   * @throws Exception
   */
  void start() throws Exception;

  /**
   * Stops Tcp server and returns all the resources.
   *
   * @throws Exception
   */
  void stop() throws Exception;

  /**
   * Handles a message received from tcp.
   *
   * @param reader
   * @param writer
   * @throws Exception
   */
  void receive(InputStream reader, OutputStream writer) throws Exception;
}
