# RestAPIStudy

## 목차

- [REST API란?](#rest)
- [HATEOAS 사용법](https://github.com/edel1212/restAPIStudy/tree/main/HATEOAS)
- [RstDocs 사용법](https://github.com/edel1212/restAPIStudy/tree/main/Spring_Rest_Docs)
- [효율적인 테스트 방법](https://github.com/edel1212/restAPIStudy/tree/main/Spring_Tips)
  
## REST?
- REST란 `REpresentational State Transfer`의 약자로 웹 기반의 소프트웨어 아키텍처 스타일 중 하나
- 인터넷 상의 시스템 간에 상호 운용성을 제공하는 방법이며, 시스템 제각각의 독립적인 진화를 보장하기 위한 방법 중 하나이다
- REST 아키텍처 스타일
  - Client-Server
  - Stateless
  - Cache
  - Uniform Interface
    - Identification of resources
    - manipulation of resources through represenations
    - 💬 self-descrive messages : ℹ️  `해당 아키텍처 스타일을 대부분 무시한다.`
    - 💬 hypermedia as the engine of appliaction state (HATEOAS - 사용하면 해결) : ℹ️  `해당 아키텍처 스타일을 대부분 무시한다.`
  - Layered System
  - Code-On-Demand (optional)

## API?
- API란 `Application Programming Interface`의 약자로 소프트웨어 응용 프로그램 간에 상호 작용 할 수 있도록 하는 인터페이스이다.
- 두개의 프로그램이 서로 통신하고 데이터를 교환할 수 있게끔 하는 규칙과 도구의 집합을 제공한다.
  - 쉽게 이해하려면 Java 진형의 Interface 또한 API라고 볼 수 있다.

#### Self-descriptive message란?

- 메시지 스스로 메시지에 대한 설명이 가능해야 한다.
- 서버가 변해서 메시지가 변해도 클라이언트는 그 메시지를 보고 해석이 가능해야한다.
- 확장 가능한 커뮤니케이션이어야 한다.
- 쉽게 이해하는 방법
  - 알맞은 `Content-Type`으로 반환해 줘야한다.
  - 알맞는 상태코드를 알려줘야한다.
  - 필드에 대한 설명 링크가 있어야한다.
- 적용 방법
  - 방법 1: 미디어 타입을 정의하고 IANA에 등록하고 그 미디어 타입을 리소스 리턴할 때 Content-Type으로 사용한다.
    - 깃헙 API의 경우 굉장히 잘되어 있다.
      - ex) header `application/vnd.github+json`
  - 방법 2: profile 링크 헤더를 추가하는 방법
    - 브라우저들이 아직 스팩 지원을 잘 안해 주므로 HAL의 링크 데이터 Body에 profile 링크 추가하는 방법을 사용한다.

  



