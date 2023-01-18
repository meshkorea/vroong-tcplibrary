package com.vroong.tcp.message.visitor;

public interface Formattable {

  /**
   * Invites a Formatter(visitor) and delegates the formatting logic to it.
   *
   * @param formatter
   */
  void accept(Formatter formatter);
}
