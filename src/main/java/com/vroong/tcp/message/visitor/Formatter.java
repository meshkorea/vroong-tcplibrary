package com.vroong.tcp.visitor;

import com.vroong.tcp.message.Packet;

public interface Formatter {

  void format(Packet formattable);
}
