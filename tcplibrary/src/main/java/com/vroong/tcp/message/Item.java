package com.vroong.tcp.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * An object that represents a fragment of a tcp message.
 *
 * e.g. If the full message is:
 *      "김메쉬  0030서버팀              클라이언트팀      "
 * "김메쉬" is an Item, "0030" is an Item, and so on.
 */
@Getter
@EqualsAndHashCode
@ToString
public class Item implements TcpMessage {

  @Setter
  private String name;
  /**
   * 문자열의 길이에 따라 조합된 전문 메시지일 때는 length를 의미함 구분자에 의해 조합된 전문 메시지일 때는 index를 의미함
   */
  private int pointer;
  @Setter
  private String value;

  public static Item of(String name, int pointer, String value) {
    return new Item(name, pointer, value);
  }

  public static Item of(String name, int pointer) {
    return new Item(name, pointer, "");
  }

  private Item(String name, int pointer, String value) {
    this.name = name;
    this.pointer = pointer;
    this.value = value;
  }
}
