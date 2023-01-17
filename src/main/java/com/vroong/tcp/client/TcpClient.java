package com.vroong.tcp.client;

public interface TcpClient {

  byte[] send(byte[] message) throws Exception;
}
