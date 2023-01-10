## Tcp Library
Tcp Server or Tcp Client를 만들기 위한 프로젝트 입니다.

```yaml
tcp:
  server:
    port: 65535
    max-connection: 100
    key-store: ${user.home}/Project/work/vroong-tcplibrary/src/main/resources/keystore-for-server.jks
    key-store-password: secret
    trust-store: ${user.home}/Project/work/vroong-tcplibrary/src/main/resources/truststore-for-server.jks
    trust-store-password: secret
  client:
    host: localhost
    port: ${tcp.server.port}
    connection-timeout: 1000 # millis
    read-timeout: 5000 # millis
    key-store: ${user.home}/Project/work/vroong-tcplibrary/src/main/resources/keystore-for-client.jks
    key-store-password: secret
    trust-store: ${user.home}/Project/work/vroong-tcplibrary/src/main/resources/truststore-for-client.jks
    trust-store-password: secret
    pool:
      min-idle: 10
      max-idle: 10
      max-total: ${tcp.server.max-connection}

```

Tcp Library는 TLS를 지원하며 인증서는 src/main/resources에 세팅되어 있습니다. (유효기간: 2023년 1월 11일 ~ 10년)
- truststore: 연결 대상의 공개키를 저장합니다.
- keystore: 공개키와 개인키를 저장합니다.
- pem: private key  
![https://docs.apigee.com/static/api-platform/images/oneWaySSLTrustStore_tls.png](https://docs.apigee.com/static/api-platform/images/oneWaySSLTrustStore_tls.png)  
- 위 그림은 one-way TLS입니다.
- two-way TLS는 서버와 클라이언트 모두 truststore와 keystore를 가지고 있습니다.
- 그림 : https://docs.apigee.com/api-platform/system-administration/about-ssl

인증서를 세팅한 방법은 다음과 같습니다.
1. 서버 keystore 생성
```shell
keytool -genkeypair -alias server \
-keyalg RSA -keypass secret -keystore keystore-for-server.jks -storepass secret -validity 3650
이름과 성을 입력하십시오.
  [Unknown]:  server
조직 단위 이름을 입력하십시오.
  [Unknown]:  meshkorea
조직 이름을 입력하십시오.
  [Unknown]:  engineering
구/군/시 이름을 입력하십시오?
  [Unknown]:  Seoul
시/도 이름을 입력하십시오.
  [Unknown]:  Seoul
이 조직의 두 자리 국가 코드를 입력하십시오.
  [Unknown]:  KR
CN=server, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR이(가) 맞습니까?
  [아니오]:  y
```
2. 서버 pem 추출
```shell
keytool -export \
-alias server -storepass secret -file server.pem -keystore keystore-for-server.jks
인증서가 <server.pem> 파일에 저장되었습니다.
```
3. 클라이언트용 truststore 생성
```shell
keytool -import \
-v -trustcacerts -alias client -file server.pem -keystore truststore-for-client.jks -keypass secret
키 저장소 비밀번호 입력:
새 비밀번호 다시 입력:
소유자: CN=server, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR
발행자: CN=server, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR
일련 번호: 1558ba71
적합한 시작 날짜: Wed Jan 11 13:50:49 KST 2023 종료 날짜: Sat Jan 08 13:50:49 KST 2033
인증서 지문:
	 SHA1: 07:75:52:7D:C6:86:D5:F0:10:89:9D:C6:BD:E2:B1:14:75:D0:EE:79
	 SHA256: F7:7E:BA:28:F3:0C:A7:8E:8D:95:38:62:6E:27:38:CD:5A:A1:9B:96:1A:28:BC:70:A1:A0:F7:E3:C1:AE:91:37
서명 알고리즘 이름: SHA256withRSA
주체 공용 키 알고리즘: 2048비트 RSA 키
버전: 3

확장:

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: CB 09 C8 C3 C5 07 8E 80   D2 A4 9D 46 52 BB 84 D0  ...........FR...
0010: 5C 27 A5 90                                        \'..
]
]

이 인증서를 신뢰합니까? [아니오]:  y
인증서가 키 저장소에 추가되었습니다.
[truststore-for-client.jks을(를) 저장하는 중]
```
4. 클라이언트 keystore 생성
```shell
keytool -genkeypair -alias client \
-keyalg RSA -keypass secret -keystore keystore-for-client.jks -storepass secret -validity 3650
이름과 성을 입력하십시오.
  [Unknown]:  client
조직 단위 이름을 입력하십시오.
  [Unknown]:  meshkorea
조직 이름을 입력하십시오.
  [Unknown]:  engineering
구/군/시 이름을 입력하십시오?
  [Unknown]:  Seoul
시/도 이름을 입력하십시오.
  [Unknown]:  Seoul
이 조직의 두 자리 국가 코드를 입력하십시오.
  [Unknown]:  KR
CN=client, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR이(가) 맞습니까?
  [아니오]:  y
```
5. 클라이언트 pem 추출
```shell
keytool -export \
-alias client -storepass secret -file client.pem -keystore keystore-for-client.jks
인증서가 <client.pem> 파일에 저장되었습니다.
```
6. 서버용 truststore 생성
```shell
keytool -import \
-v -trustcacerts -alias server -file client.pem -keystore truststore-for-server.jks -keypass secret
키 저장소 비밀번호 입력:
새 비밀번호 다시 입력:
소유자: CN=client, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR
발행자: CN=client, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR
일련 번호: 538800de
적합한 시작 날짜: Wed Jan 11 13:55:28 KST 2023 종료 날짜: Sat Jan 08 13:55:28 KST 2033
인증서 지문:
	 SHA1: 59:7D:CB:FB:C9:99:D9:74:F0:22:0C:0C:21:8A:1F:4C:67:24:82:35
	 SHA256: A8:B1:C3:F7:21:6A:28:3A:C2:23:2F:56:84:54:74:0C:11:62:75:33:E6:17:09:19:6F:97:EC:70:B4:89:13:36
서명 알고리즘 이름: SHA256withRSA
주체 공용 키 알고리즘: 2048비트 RSA 키
버전: 3

확장:

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: EC EC 4C 11 F5 C9 A0 46   63 97 36 4F 7D 76 14 41  ..L....Fc.6O.v.A
0010: 80 C8 82 0F                                        ....
]
]

이 인증서를 신뢰합니까? [아니오]:  y
인증서가 키 저장소에 추가되었습니다.
```
