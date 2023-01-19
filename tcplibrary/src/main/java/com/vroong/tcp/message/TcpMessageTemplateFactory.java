package com.vroong.tcp.message;

import java.util.List;

public interface TcpMessageTemplateFactory {

  /**
   * Provides a template of TcpMessage which consists of Items and sub Packet.
   * The shape of the object depends on the length of the original tcpMessage.
   *
   * @param tcpMessage
   * @return
   */
  List<TcpMessage> create(byte[] tcpMessage);
}
