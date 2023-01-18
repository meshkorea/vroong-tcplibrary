package com.vroong.tcp.message.visitor;

public interface Parseable {

  /**
   * Invites a Parser(visitor) and delegates the parsing logic to it.
   *
   * @param parser
   */
  void accept(Parser parser);
}
