package com.vroong.tcp.message.visitor;

import com.vroong.tcp.message.Packet;

public interface Parser {

  /**
   * Deserializes value of the tcpMessage field of the Packet
   * and then sets the Packet object's fields' value.
   *
   * @param parseable
   */
  void parse(Packet parseable);
}
