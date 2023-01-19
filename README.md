## vroong-tcplibrary

vroong-tcplibrary는 TCP 서버 또는 클라이언트를 쉽게 구현하기 위한 Java 라이브러리입니다.

### Features

- (Plain)`Socket` 및 `SSLSocket` 연결 지원
- 소켓 커넥션 풀을 이용하는 TCP 클라이언트 지원
- 메시지 끝을 식별하기 위한 전략 지원: 줄바꿈 문자 전략, 메시지 길이 헤더 전략
- Spring Boot Auto Configuration

### Dependencies
- Java 8 or higher

### Installation

프로젝트에 다음과 같이 의존을 설정합니다.

```groovy
// build.gradle
dependencies {
  implementation "com.vroong:vroongtcp-spring-boot-starter:${vroongTcpLibraryVersion}";
}
```

디버그 로그를 활성화하려면, `application.yml` 또는 logback 설정에 로그 레벨을 설정합니다.

```yaml
logging.level.com.vroong.tcp: DEBUG
```

### Usage: Client

`@Bean`으로 등록된 `TcpClient`를 사용해서 클라이언트를 구현합니다. `TcpClient`는 기본 제공되며, 직접 `@Bean`으로 등록하여 커스텀할 수 있습니다.

```java
@Service
public class YourService {

  private final TcpClient tcpClient;

  public YourService(TcpClient tcpClient) {
    this.tcpClient = tcpClient;
  }

  public String yourServiceMethod(String message) {
    byte[] response = tcpClient.send(message.getBytes());
    return new String(response);
  }
}
```

### Usage: Server<a id="server-usage"></a>

`AbstractTcpServer`를 구현합니다. TLS를 사용하는 경우 반드시 인증서 위치를 `application.yml`에 명시해야 하며, `YourTcpServer(TcpServerProperties properties, HeaderStrategy headerStrategy, Boolean useTLS, Boolean needClientAuth)` 생성자를 사용해야 합니다.

```java
@Component
public class YourTcpServer extends AbstractTcpServer {
  
  public YourTcpServer(TcpServerProperties properties, HeaderStrategy headerStrategy) {
    super(properties, headerStrategy, true, true);
//    super(properties, headerStrategy, true, false);
//    super(properties, headerStrategy, false, false);
  }
  
//  public YourTcpServer(TcpServerProperties properties) {
//    super(properties);
//  }
  
  @Override
  public byte[] receive(byte[] received) {
    byte[] response = null;
    // your implementation here to set the value of response variable...
    return response;
  }
}
``` 
- Spring Container에 등록할 수 있다면 어느 방법이든 가능합니다.

`SpringBootApplication` 구동시 `TcpServer`도 함께 구동해야 하며, 아래와 같은 방법으로 구동할 수 있습니다.

방법 1
```java
@Component
public class TcpServerStarter implements ApplicationRunner {
  
  TcpServer tcpServer;

  public TcpServerStarter(TcpServer tcpServer) {
    this.tcpServer = tcpServer;
  }
  
  @Override
  public void run(ApplicationArguments args) throws Exception {
    tcpServer.start();
  }
}
```

방법 2
```java
@Configuration
public class TcpServerConfiguration {

  @Bean
  public ApplicationRunner applicationRunner(YourTcpServer yourTcpServer) {
    return args -> yourTcpServer.run();
  } 
}
```

### Configuration

#### Bean

Bean|Required|Default
---|---|---
`TcpServer`<sup>(1)</sup>|true|N/A 
`TcpClient`<sup>(2)</sup>|false|`DisposableTcpClient`
`HeaderStrategy`|false|`NullHeaderStrategy`

(1) `AbstractTcpServer`를 구현해야 하여 `@Bean`으로 등록해야 합니다. [Usege: Server](#server-usage) 참조  
(2) TLS를 사용하는 경우 인증서 위치를 `application.yml`에 명시해야 하며, `xxxTcpClient(TcpClientProperties properties, HeaderStrategy strategy, Boolean useTLS)` 생성자를 사용해서 `TcpClient`를 `@Bean`으로 등록해야 합니다.

```java
@Configuration
public class TcpConfiguration {

  @Bean
  public TcpServer tcpServer(TcpServerProperties properties, HeaderStrategy headerStrategy) {
    // (Plain)
    return new YourTcpServer(properties, headerStrategy, false);

    // (Plain)
    // return new YourTcpServer(properties);

    // (Secure): server authentication
    // return new YourTcpServer(properties, headerStrategy, true, false);

    // (Secure): mutual authentication
    // return new YourTcpServer(properties, headerStrategy, true, true);
  }

  @Bean
  public TcpClient tcpClient(TcpClientProperties properties, HeaderStrategy strategy) {
    // (Plain)
    return new DisposableTcpClient(properties, strategy, false);

    // (Plain)
    // Socket Pool을 사용하는 경우 PooledTcpClient를 사용
    // return new PooledTcpClient(properties, strategy, false);

    // (Plain)
    // 메시지가 줄바꿈 문자로 종료되는 케이스 전략을 사용하는 TcpClient
    // return new DisposableTcpClient(properties);
    // return new PooledTcpClient(properties);

    // (Secure)
    // return new DisposableTcpClient(properties, strategy, true);

    // (Secure)
    // Socket Pool을 사용하는 경우 PooledTcpClient를 사용
    // return new PooledTcpClient(properties, strategy, true);
  }
  
  @Bean
  public HeaderStrategy headerStrategy() {
    // 메시지 헤더에 전체 메시지의 길이가 명시된 케이스; 패딩 문자: '0', 헤더 길이: 4, 인코딩: utf-8 
    return new LengthAwareHeaderStrategy('0', 4, StandardCharsets.UTF_8);
    
    // 메시지가 줄바꿈 문자로 종료되는 케이스
    // return new NullHeaderStrategy();
    
    // HeaderStrategy를 직접 구현한 커스텀 케이스
    // return new YourCustomStrategy();
  }
}
```

#### Configuration Properties

```yaml
# (your project root)/src/main/resources/application.yml

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

#### Self-signed Certificate for SSLSocket

[tcplibrary/src/main/resources/CERTIFICATE.md](tcplibrary/src/main/resources/CERTIFICATE.md) 참조

### Contribution

이슈, PR 보내주세요.

#### Publishing Jar

Nexus에 발행하기 전에 `build.gradle`을 열어 `version` 프로퍼티의 값을 변경해주세요. 

```shell
$ ./gradlew publish
```
