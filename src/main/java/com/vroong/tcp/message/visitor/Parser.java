package com.vroong.tcp.visitor;

import com.vroong.tcp.message.Packet;

public interface Parser {

  void parse(Packet parseable);
}
