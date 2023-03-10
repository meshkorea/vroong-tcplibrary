package com.vroong.tcp.server;

public interface TcpServer {

  /**
   * Starts a Tcp server, which blocks indefinitely waiting for a client connection.
   * Once a client is connected, a new worker thread will be allocated.
   *
   * @throws Exception
   */
  void start() throws Exception;

  /**
   * Stops the Tcp server and returns all the resources.
   *
   * @throws Exception
   */
  void stop() throws Exception;

  /**
   * Handles a message received from tcp.
   *
   * @param received received byte array
   * @return byte array to respond
   * @throws Exception
   */
  byte[] receive(byte[] received) throws Exception;
}
