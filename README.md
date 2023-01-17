## Tcp Library

Tcp Server or Tcp Client를 만들기 위한 프로젝트 입니다.

### Configuration Properties

```yaml
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
