package com.vroong.tcp.message;

public interface TcpMessage {

  /**
   * Name for a fragment of the TcpMessage.
   *
   * e.g. "root", "name", "age", ...
   *
   * @return
   */
  String getName();

  void setName(String name);

  /**
   * Length of the value when the TcpMessage was constructed as a fixed-length byte
   * or index number of the value when the TcpMessage was constructed by a delimiter.
   *
   * @return
   */
  int getPointer();

  /**
   * A fragment of the TcpMessage.
   *
   * @return
   */
  String getValue();

  void setValue(String value);
}
