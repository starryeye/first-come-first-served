# first-come-first-served
선착순 이벤트

## 요구사항
- 쿠폰 발급은 최대 100개 까지만 발급한다.
- 쿠폰 발급은 1인당 1개로 제한한다.

## project
- api
  - Redis Set data type 을 활용한 쿠폰 발급 유저 유니크 체크
  - Redis INCR 명령어 사용
    - 쿠폰 발급 갯수 counting 하여 동시성 문제 해결
    - 쿠폰 발급 totalCount 쿼리 DB 부담 해소
  - Kafka 쿠폰 발급 비동기 처리
    - 쿠폰 발급(DB 저장) 로직을 kafka 메시지 produce 로 대체(비동기)
    - DB 적재 로직이 동기 I/O 로 진행되는 부담(타임아웃 등) 을 없앰
- consumer
  - Kafka 쿠폰 발급 이벤트 consumer 하여 쿠폰 발급(DB 저장)
  - 쿠폰 발급 이벤트를 소비함에 있어서 실패할 경우 실패 DB 저장


## dependency
- Spring Boot
- Spring Web
- Spring Data Jpa
- Spring Data Redis
- Spring Kafka
- MySQL
- lombok
