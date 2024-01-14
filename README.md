# restAPIStudy

## REST_API 란?

- REST 아키텍처 스타일을 따르는 API


### API?

- API란  `Application Programming Interface`의 약자로 소프트웨어 응용 프로그램 간에 상호 작용 할 수 있도록 하는 인터페이스이다.
- 두개의 프로그램이 서로 통신하고 데이터를 교환할 수 있게끔 하는 규칙과 도구의 집합을 제공한다.
  - 쉽게 이해하려면 Java 진형의 Interface 또한 API라고 볼 수 있다. 


### REST?

- REST란 `REpresentational State Transfer`의 약자로 웹 기반의 소프트웨어 아키텍처 스타일 중 하나입니다.
- 인터넷 상의 시스템 간에 상호 운용성을 제공하는 방법이며, 시스템 제각각의 독립적인 진화를 보장하기 위한 방법 중 하나이다 
- REST 아키텍처 스타일
  - Client-Server
  -  Stateless
  -  Cache
  -  Uniform Interface
      - Identification of resources
      -  manipulation of resources through represenations
      - 💬 self-descrive messages  : `해당 아키텍처 스타일을 대부분 무시한다.`
      - 💬 hypermedia as the engine of appliaction state (HATEOAS) : `해당 아키텍처 스타일을 대부분 무시한다.`
  -  Layered System
  -  Code-On-Demand (optional)

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

#### HATEOAS란?

- `Hypermedia As The Engine Of Application State`의 약어로, RESTful API에서 클라이언트와 서버 간의 상호 작용을 위해 하이퍼미디어를 사용하는 아키텍처 스타일을 나타냅니다.
- 쉽게 이해하는 방법
  - 응답에 따라 동적인 링크를 사용하여 API를 더 자기 기술적으로 만들어준다.
  ```javascript
  /** HATEOAS스러운 응답  */
  {
  "id": 123,
  "name": "John Doe",
  "links": [
    {
      "rel": "self",
      "href": "/api/users/123"
    },
    {
      "rel": "edit",
      "href": "/api/users/123/edit"
    },
    {
      "rel": "delete",
      "href": "/api/users/123/delete"
    }
   ]
  }
  ```


## 프로젝트 의존성 목록
- Web
- JPA
- HATEOAS
- REST Docs
- H2
- PostgreSQL
- Lombok


### Dependencies 스코프 관련
- Test Scope로 설정 시 프로젝트가 실행 시 해당 디팬던시는 사용되지 않는다.
   - 따라서 테스트에만 사용할 의존성의 경우 스코프를 변경하여 개발하는 습관을 들이자.
- ex) h2 Database의 경우 테스트로만 사용 예정
  - Maven
    ```xml
    <deepndency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <!--    <scope>runtime</scope> -->
      <!-- 변경 -->
      <scope>test</scope>
    </deepndency>
    ```
  - Gradle
    ```groovy
    dependencies {
       //runtimeOnly 'com.h2database:h2'
       /** 변경  */
       testImplementation 'com.h2database:h2'
    }
    ```
| 스코프          | Maven 설정                           | Gradle 설정                               | 설명                                       |
|-----------------|--------------------------------------|--------------------------------------------|--------------------------------------------|
| 컴파일          | `<scope>compile</scope>`              | `implementation 'group:artifact:version'`  | 모든 빌드 단계에서 사용되는 기본적인 의존성 범위   |
| 컴파일 전용     | `<scope>provided</scope>`             | `compileOnly 'group:artifact:version'`     | 컴파일 시에만 사용되며, 런타임에서는 제외됨       |
| 런타임 전용     | `<scope>runtime</scope>`              | `runtimeOnly 'group:artifact:version'`     | 런타임 시에만 사용되며, 컴파일 시에는 사용되지 않음 |
| 시스템          | `<scope>system</scope>`               | 사용하지 않음 (사용 시에는 추가 설정 필요) | 시스템에 직접 설치된 JAR 파일과 같은 외부 JAR 파일에 대한 의존성 |
| 테스트          | `<scope>test</scope>`                 | `testImplementation 'group:artifact:version'`| 테스트 코드에서만 사용되는 의존성               |
| 어노테이션 프로세서 | `<scope>compile</scope>` + 어노테이션 프로세서 플러그인 | `annotationProcessor 'group:artifact:version'` | 컴파일 시에만 사용되는 어노테이션 프로세서     |



## 테스트 코드 작성
- 


<hr/>

## 유용한 intellij 단축키
- `커맨드 + 쉬프트 + t` :  사용 클래스에서의 테스트 코드 생성 및 이동이 가능함
- `컨트롤 + 옵션 + o` : 사용하지 않는 import 자동 삭제
- `알트 + 엔터`  : 자동 import 
- `알트 + 커맨드 + v` : 중복 값을 추출하여 변수로 만들어 줌
- `커맨드 + 쉬프트 + v` : 과거 복사 이력 사용 가능 윈도우 클립 보드 기능 유사