package com.vroong.tcp.visitor;

import com.vroong.tcp.Packet;

public interface Parser {

  void parse(Packet parseable);
}
