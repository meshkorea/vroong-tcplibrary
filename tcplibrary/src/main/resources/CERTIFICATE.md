## Self-signed Certificate

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
$ keytool -genkeypair -alias server \
  -keyalg RSA -keypass secret -keystore keystore-for-server.jks -storepass secret -validity 3650
# 이름과 성을 입력하십시오.
#  [Unknown]:  server
# 조직 단위 이름을 입력하십시오.
#  [Unknown]:  meshkorea
# 조직 이름을 입력하십시오.
#  [Unknown]:  engineering
# 구/군/시 이름을 입력하십시오?
#  [Unknown]:  Seoul
# 시/도 이름을 입력하십시오.
#  [Unknown]:  Seoul
# 이 조직의 두 자리 국가 코드를 입력하십시오.
#  [Unknown]:  KR
# CN=server, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR이(가) 맞습니까?
#  [아니오]:  y
```

2. 서버 pem 추출
```shell
$ keytool -export \
  -alias server -storepass secret -file server.pem -keystore keystore-for-server.jks
# 인증서가 <server.pem> 파일에 저장되었습니다.
```

3. 클라이언트용 truststore 생성
```shell
$ keytool -import \
  -v -trustcacerts -alias client -file server.pem -keystore truststore-for-server.jks -keypass secret
# 키 저장소 비밀번호 입력:
# 새 비밀번호 다시 입력:
# 경고: 다른 저장소 및 키 비밀번호는 PKCS12 KeyStores에 대해 지원되지 않습니다. 사용자가 지정한 -keypass 값을 무시하는 중입니다.
# 소유자: CN=server, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR
# 발행자: CN=server, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR
# 일련 번호: 6d2c3d35
# 적합한 시작 날짜: Fri Jan 13 14:30:23 KST 2023 종료 날짜: Mon Jan 10 14:30:23 KST 2033
# 인증서 지문:
# 	 SHA1: D3:0D:91:2F:3A:26:5C:83:52:16:B8:E4:9D:FA:D1:99:62:F5:E1:48
# 	 SHA256: 2D:5A:A2:3B:2C:CC:9B:9C:99:FE:1F:4B:54:C2:86:D6:B9:C1:F8:44:A3:3D:7F:AD:1B:87:6A:71:BE:FA:74:00
# 서명 알고리즘 이름: SHA256withRSA
# 주체 공용 키 알고리즘: 2048비트 RSA 키
# 버전: 3
# 
# 확장:
# 
# #1: ObjectId: 2.5.29.14 Criticality=false
# SubjectKeyIdentifier [
# KeyIdentifier [
# 0000: DC 3A 56 46 C8 C4 D0 E3   3B 04 1D AD 1F EE 46 08  .:VF....;.....F.
# 0010: EB CC 1B 4D                                        ...M
# ]
# ]
# 
# 이 인증서를 신뢰합니까? [아니오]:  y
# 인증서가 키 저장소에 추가되었습니다.
# [truststore-for-client.jks을(를) 저장하는 중]
```

4. 클라이언트 keystore 생성
```shell
$ keytool -genkeypair -alias client \
  -keyalg RSA -keypass secret -keystore keystore-for-client.jks -storepass secret -validity 3650
# 이름과 성을 입력하십시오.
#   [Unknown]:  client
# 조직 단위 이름을 입력하십시오.
#   [Unknown]:  meshkorea
# 조직 이름을 입력하십시오.
#   [Unknown]:  engineering
# 구/군/시 이름을 입력하십시오?
#   [Unknown]:  Seoul
# 시/도 이름을 입력하십시오.
#   [Unknown]:  Seoul
# 이 조직의 두 자리 국가 코드를 입력하십시오.
#   [Unknown]:  KR
# CN=client, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR이(가) 맞습니까?
#   [아니오]:  y
```

5. 클라이언트 pem 추출
```shell
$ keytool -export \
  -alias client -storepass secret -file client.pem -keystore keystore-for-client.jks
# 인증서가 <client.pem> 파일에 저장되었습니다.
```

6. 서버용 truststore 생성
```shell
$ keytool -import \
  -v -trustcacerts -alias server -file client.pem -keystore truststore-for-client.jks -keypass secret
# 키 저장소 비밀번호 입력:
# 새 비밀번호 다시 입력:
# 경고: 다른 저장소 및 키 비밀번호는 PKCS12 KeyStores에 대해 지원되지 않습니다. 사용자가 지정한 -keypass 값을 무시하는 중입니다.
# 소유자: CN=client, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR
# 발행자: CN=client, OU=meshkorea, O=engineering, L=Seoul, ST=Seoul, C=KR
# 일련 번호: 5dd83ba1
# 적합한 시작 날짜: Fri Jan 13 14:33:18 KST 2023 종료 날짜: Mon Jan 10 14:33:18 KST 2033
# 인증서 지문:
# 	 SHA1: D3:24:36:74:14:1E:E4:19:7F:CF:35:9B:6D:B2:57:F9:47:16:2D:7A
# 	 SHA256: D2:CC:06:45:B8:1A:7F:60:19:A4:12:26:51:DF:04:66:67:B1:11:BB:89:29:E5:08:DB:E9:3A:F5:E7:5B:E9:09
# 서명 알고리즘 이름: SHA256withRSA
# 주체 공용 키 알고리즘: 2048비트 RSA 키
# 버전: 3
# 
# 확장:
# 
# #1: ObjectId: 2.5.29.14 Criticality=false
# SubjectKeyIdentifier [
# KeyIdentifier [
# 0000: 8D 39 DF F9 E7 FB 78 B5   79 70 86 4A DB 35 78 4C  .9....x.yp.J.5xL
# 0010: 23 B2 9D 7F                                        #...
# ]
# ]
# 
# 이 인증서를 신뢰합니까? [아니오]:  y
# 인증서가 키 저장소에 추가되었습니다.
# [truststore-for-server.jks을(를) 저장하는 중]
```
