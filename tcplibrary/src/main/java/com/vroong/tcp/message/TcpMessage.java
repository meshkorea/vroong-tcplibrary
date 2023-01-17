package com.vroong.tcp.message;

public interface TcpMessage {

  String getName();

  void setName(String name);

  int getPointer();

  String getValue();

  void setValue(String value);
}
