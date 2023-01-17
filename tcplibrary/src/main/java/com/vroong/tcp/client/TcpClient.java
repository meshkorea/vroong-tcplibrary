package com.vroong.tcp.client;

public interface TcpClient {

  /**
   * Sends a message via tcp.
   *
   * @param message byte array to send
   * @return byte array received
   * @throws Exception
   */
  byte[] send(byte[] message) throws Exception;
}
