package com.vroong.tcp.message.visitor;

import com.vroong.tcp.message.Packet;

public interface Formatter {

  /**
   * Serializes the object values of the Packet into a byte array
   * and then sets the produced byte array to the tcpMessage field.
   *
   * @param formattable
   */
  void format(Packet formattable);
}
