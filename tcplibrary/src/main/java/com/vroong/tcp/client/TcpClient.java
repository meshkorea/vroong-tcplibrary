package com.vroong.tcp.client;

public interface TcpClient {

  /**
   * Sends a message via tcp.
   *
   * @param message to send
   * @return message received
   * @throws Exception
   */
  String send(String message) throws Exception;
}
