## Tcp Library

Tcp Server or Tcp Client를 만들기 위한 프로젝트 입니다.

### Terminology

#### TcpMessage

`TcpMesasge`는 TCP를 통해 교환하는 메시지를 객체로 표현한 것입니다. 컴포지트 패턴을 이용합니다. `Item`은 TCP 메시지의 각 조각을 의미하고, `Item` 또는 `Packet`의 조합을 `Packet`으로 표현합니다. 

가령, `김메쉬  0030서버팀              클라이언트팀      `이란 TCP 메시지를 `TcpMessage`로 표현하면 다음과 같습니다.

구분|타입|길이
---|---|---
이름|문자|12
나이|숫자|4(Left 0 pad)
팀[]|문자|24


```json
Packet {
  Item {
    name = "name"
    pointer = 12
    value = "김메쉬"
  }
  Item {
    name = "age"
    pointer = 4
    value = 30
  }
  Packet {
    Item {
      name = "team0"
      pointer = 24
      value = "서버팀"
    }
    Item {
      name = "team1"
      pointer = 24
      value = "클라이언트팀"
    }
  } 
}
```

![](https://plantuml-server.kkeisuke.dev/svg/TP1D2y8m38Rl_HLX9p_OqFiOWYT1HL2ybouRnNwC9VCY_dSxrx4XU4lUlddofZjYsN9dZH3QCRQLL0WtrPoGIDO8UQwyRRn3nqK1Bm6mXYktsjLG8v-bnSNoApwzBe6Y51qkrCDhOFV-Y_ykYICJvovDzmiQjEXvzirIl8LGHXB1HQe7yfYED6D8fsrZq8QaDB-Wpvp2KHFdarg07OhzU1rDy0EZ3MY3ilfF3UuO8jacuDEkIGj8DbcsIcO-8NReol2z7m00.svg)

#### Parser, Formatter, TcpMessageTemplateFactory

`Parser`는 TCP로 받은 메시지를 해석하여 `Packet` 객체를 생성하는 역할을 합니다. `Formatter`는 `Packet` 객체를 TCP로 보내기 위해 바이트 배열로 포맷팅하는 역할을 합니다. `TcpMessageTemplateFactory`는 `Parser`가 TCP로 받은 메시지를 해석하여 `Packet` 객체를 만들기 위한 템플릿을 제공합니다.

![](https://plantuml-server.kkeisuke.dev/svg/bLBDQiCm3BxxANJCsD8d2APGw6c37deREwYkMipYsCXQeOoz-np7QKhRC7QoyAVlHxANKP3boxLAsKw83sW8rfuTYk2k9VXM0CzmydOFQ0m5gGfA38UnUb8_3_YfesbEDbhnhFMrqa2iDcW-IKQPtkv6Q8CSQIP6nXX2FXxn6ERiCsTq3wD7rZ0jnWW5AlGUJ0BMXPOSTHAdN2ztkwhXrKPfjYQyKOnu90rkA5RU1T_r_Bzc-V_69xijfPWej4QJvZwNiUyo6QRKLEtEGkyV81Sawweo_sAXfxUylXpogXAxhlLy0AMMrEtJ1_i5.svg)


#### HeaderStrategy

`HeaderStrategy`는 Tcp 메시지를 보내고 받을 때, 메시지 헤더를 어떻게 구성할 것인가에 대한 전략입니다. `NullHeaderStrategy`는 줄바꿈 문자로 메시지가 구분된다고 가정합니다. `LengthAwareHeaderStrategy`는 메시지 본문 앞에 헤더 자신의 길이를 포함한 전체 메시지 길이를 명시하고 있다고 가정합니다.

가령, "안녕하세요\n"와 같은 메시지를 주고 받으려면 `NullHeaderStrategy`를 사용하고, 메시지가 "0019안녕하세요"와 같다면 `LengthAwareHeaderStrategy`를 사용할 수 있습니다. 기본 구현에서 제공하지 않는 전략이 필요하다면 `HeaderStrategy`를 직접 구현할 수 있습니다.

![](https://plantuml-server.kkeisuke.dev/svg/ZL91RiCW4BppYZtQQlo5AbKkhLIrX_HMvK1WQoDXZ9Qr8olAtqlNhKoY77e2PfUPsO5jOaqyzbuf5nZfInk4PzGMQS-a6TiPpWhW0Oupu-S1ADUgbn17pdNKlG18jVHCWxDm8iTOlO-yzWX4cp-Eus3dMbsKCbwd1AyOMkwUJfhmHjdrqMEFWTCzJmtaemij-AZjhjCaM1u3EcbUU0NygIPaCCkwQh-DtlzVz5Acz67Y7cb2-Wr8XfSugxe4XbYU2Gsw2Si8Dr269S5SgUoyoswkdo5XiDMobmkhcQPvlF6jLeuxHRNDLFa3V0C0.svg)


### Message Security

TCP를 통해 메시지를 주고 받을 때, 발신자와 수신자의 신원 확인 및 메시지를 보호하기 위한 방법은 아래와 같습니다.

1. Plain Socket: 발신자와 수신자의 신원 확인을 하지 않고, 메시지를 암호화하지 않고 통신하는 방식 (보안 안됨)
2. Server Authentication: 서버의 공개 키를 클라이언트에게 제공하고, 클라이언트가 서버가 제공한 공개 키를 이용하여 메시지를 주고 받는 방식 (일반적인 브라우저 통신에서 사용됨)
3. Mutual Authentication: 서버뿐만 아니라, 클라이언트의 공개 키도 서버에 제공하여 상호 인증을 한 후 메시지를 주고 받는 방식

### Usage: Client

#### Plain Socket

```java
final TcpClient client = new DisposableTcpClient(new TcpClientProperties());
// 메시지가 줄바꿈 문자로 끝나는 케이스
// final TcpClient client = new DisposableTcpClient(new TcpClientProperties(), new NullHeaderStrategy(), false);
// 메시지 헤더에 전체 메시지의 길이가 명시된 케이스
// final TcpClient client = new DisposableTcpClient(new TcpClientProperties(), new LengthAwareHeaderStrategy(), false);

final String message = "안녕하세요?";
final byte[] response = client.send(message.getBytes());
```
- Socket Pool을 사용하는 경우 `TcpClient` 구현체를 `PooledTcpClient`를 사용


#### Secure Socket

```java
final TcpClient client = new DisposableTcpClient(new TcpClientProperties(), new NullHeaderStrategy(), true);
// 메시지 헤더에 전체 메시지의 길이가 명시된 케이스
// final TcpClient client = new DisposableTcpClient(new TcpClientProperties(), new LengthAwareHeaderStrategy(), true);

final String message = "안녕하세요?";
final byte[] response = client.send(message.getBytes());
```
- `PooledTcpClient`를 사용하는 경우 `TcpClient` 구현체를 `PooledTcpClient`를 사용

### Usage: Server

#### Plain Socket

```java
public class YourTcpServer extends AbstractTcpServer {
  public YourTcpServer(TcpServerProperties properties) {
    super(properties);
  }
  
  @Override
  public byte[] receive(byte[] received) {
    byte[] response = null;
    // Your implementation here...
    return response;
  }
}
```
- `com.vroong.tcp.server.example.EchoServer` 구현 참조

#### Secure Socket

```java
public class YourSecureTcpServer extends AbstractTcpServer {
  public YourSecureTcpServer(TcpServerProperties properties) {
    super(properties, new NullHeaderStrategy(), true, true);
  }

  @Override
  public byte[] receive(byte[] received) {
    byte[] response = null;
    // Your implementation here...
    return response;
  }
}
```
- `com.vroong.tcp.server.example.EchoServerWithTLS` 구현 참조

### Configuration Properties

```yaml
# tcplibrary/src/main/resources/application.yml

tcp:
  server:
    port: 65535
    maxConnection: 100
    keyStore: ${user.dir}/src/main/resources/keystore-for-server.jks
    keyStorePassword: secret
    trustStore: ${user.dir}/src/main/resources/truststore-for-server.jks
    trustStorePassword: secret
  client:
    host: localhost
    port: 65535
    connectionTimeout: 1000 # millis
    readTimeout: 5000 # millis
    keyStore: ${user.dir}/src/main/resources/keystore-for-client.jks
    keyStorePassword: secret
    trustStore: ${user.dir}/src/main/resources/truststore-for-client.jks
    trustStorePassword: secret
    pool:
      minIdle: 10
      maxIdle: 10
      maxTotal: 100
```

### Self-signed Certificates

- [tcplibrary/src/main/resources/CERTIFICATE.md](src/main/resources/CERTIFICATE.md) 참조

name|key pass|valid for
---|---|---
server.pem|secret|10yrs
client.pem|secret|10yrs

name|store pass
---|---
keystore-for-server|secret
truststore-for-server|secret
keystore-for-client|secret
truststore-for-client|secret
