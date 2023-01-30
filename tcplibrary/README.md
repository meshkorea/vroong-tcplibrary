## Tcp Library

TCP 서버 또는 클라이언트를 쉽게 구현하기 위한 Java 라이브러리입니다.

```shell
tcplibrary/src/main/java/com/vroong/tcp
├── client  # 클라이언트 패키지
├── message # Tcp로 주고 받는 메시지 관련 패키지
└── server  # 서버 패키지
```

### Terminology

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
