package com.vroong.tcp.message.visitor;

public interface Parseable {

  void accept(Parser parser);
}
