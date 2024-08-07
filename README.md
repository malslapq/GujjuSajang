<h2> 목차 </h2>

- [기술 스택](#기술-스택)
- [ERD](#ERD)
- [API 명세](#api-명세)
- [주요 기능](#주요-기능)
- [아키텍처](#아키텍처)
- [기술적 의사 결정](#기술적-의사-결정)
- [성능 개선](#성능-개선)
- [트러블 슈팅](#트러블-슈팅)

---

<h3> 프로젝트 소개  </h3>


<img alt="로고" src="resource/로고.png" width=100%>

- 사용자가 다양한 굿즈를 한 곳에서 검색 구입이 가능하며 한정판 굿즈를 선착순으로 구매할 수 있고 누구든 판매자 등록을 통해 자신이 제작한 굿즈를 판매할 수 있는 서비스를 제공하는 굿즈 이커머스 프로젝트입니다.

<h3 id="기술-스택"> 기술 스택 </h3>
<div>
    <img src="https://img.shields.io/badge/java 17-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white">
    <img src="https://img.shields.io/badge/Spring Boot 3.3.1 -6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white">
    <img src="https://img.shields.io/badge/spring Data jpa-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white">
    <img src="https://img.shields.io/badge/Apache %20Kafka 3.5-000?style=for-the-badge&logo=apachekafka">
    <img src="https://img.shields.io/badge/mysql 8.0 -4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white">
    <img src="https://img.shields.io/badge/redis 7.2.4-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white">
    <img src="https://img.shields.io/badge/docker 26.1.1-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white">
    <img src="https://img.shields.io/badge/Gradle 8.8-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white">
</div>

<h3>
<details>
    <summary>ERD</summary>
    <img alt="ERD" src="resource/ERD.png"/>
</details>
</h3>


<h3 id="api-명세"> API 명세 </h3>

[API 명세서](https://documenter.getpostman.com/view/11696446/2sA3kSo3FV)

<h3 id="주요-기능"> 주요 기능 </h3>

<details>
    <summary>장바구니</summary>
    <img alt="장바구니 시퀀스 다이어그램" src="resource/장바구니 시퀀스 다이어그램.png"/>

- 사용자가 원하는 제품을 장바구니에 담거나, 담은 물건을 수정 및 삭제할 수 있으며, 변경된 날로부터 7일 동안 유지됩니다.

</details>

<details> 
    <summary> 선착순 구매 </summary> 
    <img alt="주문 시퀀스 다이어그램" src="resource/주문 시퀀스 다이어그램.png"/>

- 특정 시간부터 주문할 수 있도록 구현했으며 이벤트 기반으로 진행되고 실패 시 보상 트랜잭션을 통해 자동으로 재고를 복구합니다.

</details>

<details> 
    <summary> 실시간 재고 확인 </summary>
    <img alt="실시간 재고 확인 시퀀스 다이어그램" src="resource/실시간 재고 확인 시퀀스 다이어그램.png"/>

- 제품 ID를 통해 서버와 SSE 통신을 설정해 사용자가 실시간으로 재고 상태를 모니터링할 수 있는 기능입니다.

</details>


<h3 id="아키텍처"> 아키텍처 </h3>

<img alt="아키텍처" src="resource/아키텍처.png"/>

---

<h3 id="성능-개선"> 성능 개선 </h3>

<b> 재고 관리 </b>

초기 상태에서는 모든 재고 조회 및 갱신 작업이 데이터베이스에 직접 접근하는 방식으로 이루어졌습니다. 
이로 인해 많은 부하가 발생하고 응답 시간이 길어져 개선이 필요하다고 느꼈고 성능을 최적화하기 위해 캐싱과 Lua 스크립트를 사용하여 재고 처리를 개선했습니다.

성능 개선 단계 (Jmeter 500개 쓰레드, 10번의 루프 테스트)

- 캐싱 적용 전
  <img alt="캐싱안함" src="resource/캐싱안함.png"/>
- 캐싱 적용, Redis를 사용하여 재고 데이터를 캐싱함으로써 데이터베이스 조회 빈도를 줄임
- <b> 평균 응답 시간 52.7% 개선 </b>
  <img alt="캐싱했을 때" src="resource/캐싱했을 때.png"/>
- 캐싱 적용 및 Lua 스크립트를 사용, 원자적처리 및 네트워크 왕복 시간을 줄임
- <b> 평균 응답 시간 73.2% 개선 </b>
  <img alt="루아스크립트 적용" src="resource/루아스크립트 적용.png"/>
  <img alt="성능 개선 그래프" src="resource/성능 개선 그래프.png"/>

---

<h3 id="기술적-의사-결정"> 기술적 의사 결정 </h3>

<b> Message Driven과 Event Driven </b>

- 이커머스 프로젝트를 진행하며 Event Driven Architecture 를 적용했는데 그 이유로는
    - Message Driven은 동기식 통신을 기반으로 하기 때문에 요청에 대한 응답이 오기전까지 대기하고 있어야 하기 때문에 전체적인 응답시간이 길어질 수 있는 반면 Event Driven은 이벤트 기반
      비동기 통신을 사용할 수 있기에 여러 작업들을 동시에 처리할 수 있어 전체적 응답 속도를 높일 수 있습니다.
    - Message Driven은 서비스들간에 강한 결합이 생길 수 있습니다. 강한 결합이 생길 경우 유지 보수성과 확장성이 떨어지게 되는 반면 Event Driven의 경우 이벤트 Pub/Sub을 통해 서비스
      로직을 실행하기 때문에 각 서비스 간의 결합도를 낮춰 Message Driven에 비해 높은 확장성과 더 나은 유지 보수성을 가질 수 있습니다.
- 결론적으로 이커머스 프로젝트는 사용자에게 빠르게 반응해야 하며 높은 확장성과 유연성이 필요한데 이런 요구사항을 충족시키기 위해 Message Driven Architecture보다 Event Driven
  Architecture가 더 적합하다고 판단하여 선택했습니다.

<b> 재고 캐싱 </b>

- 성능 개선을 위해 재고를 캐싱하는 방법을 선택했고, 캐시를 적용하기 전에 제일 중요하게 생각한 것은 캐시 데이터와 DB 데이터에 대한 정합성과 일관성을 맞춰주는 것   
- 조회
  1. DB에서 데이터를 미리 캐싱
  2. 캐시 데이터가 없으면 DB에서 읽어오고 캐싱하고 응답
  - 자주 사용되지 않는 데이터를 캐싱해놓는 리소스가 아깝다고 판단 => 두번째 방법으로 적용했고 TTL을 짧게 주어 정합성, 일관성을 챙기려고 함
- 차감
  1. 먼저 캐시 데이터를 수정하고 즉시 반영
  2. 일정 시간이나 일정 데이터가 쌓일 경우 DB에 반영
  - 2번의 경우 일괄적인 처리로 인해 DB에 부하를 줄일 수 있으나 정합성과 일관성이 깨질 수 있다고 판단 => 첫번째 방법으로 적용하되 쿼리문을 작성해 Entity를 조회하지 않고 제품의 ID로 즉시 업데이트

---

<h3 id="트러블-슈팅"> 트러블 슈팅 </h3>

[동시성 이슈](https://velog.io/@malslapq/%EB%AC%BC%EA%B1%B4%EC%9D%B4-%EC%97%86%EB%8A%94%EB%8D%B0-%ED%8C%94%EB%A0%A4%EB%B2%84%EB%A0%B8%EB%8B%A4...-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%9D%B4%EC%8A%88)
