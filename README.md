## Tcp Library

Tcp Server or Tcp Client를 만들기 위한 프로젝트 입니다.

### Configuration Properties

```yaml
tcp:
  server:
    port: 65535
    max-connection: 100
    key-store: ${user.dir}/src/main/resources/keystore-for-server.jks
    key-store-password: 8NKyCe
    trust-store: ${user.dir}/src/main/resources/truststore.jks
    trust-store-password: yr9GO0
  client:
    host: localhost
    port: ${tcp.server.port}
    connection-timeout: 1000 # millis
    read-timeout: 5000 # millis
    key-store: ${user.dir}/src/main/resources/keystore-for-client.jks
    key-store-password: zqLbgd
    trust-store: ${user.dir}/src/main/resources/truststore.jks
    trust-store-password: yr9GO0
    pool:
      min-idle: 10
      max-idle: 10
      max-total: ${tcp.server.max-connection}

```

### Self-signed Certificates

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
