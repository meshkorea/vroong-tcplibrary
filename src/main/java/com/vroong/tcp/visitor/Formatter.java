package com.vroong.tcp.visitor;

import com.vroong.tcp.Packet;

public interface Formatter {

  void format(Packet formattable);
}
