# restAPIStudy

## 목차
- [REST_API 란?](#REST_API 란?)
- [REST_API 란?](## 개발 시 참고 사항)
### REST_API 란?

- REST 아키텍처 스타일을 따르는 API

### API?

- API란 `Application Programming Interface`의 약자로 소프트웨어 응용 프로그램 간에 상호 작용 할 수 있도록 하는 인터페이스이다.
- 두개의 프로그램이 서로 통신하고 데이터를 교환할 수 있게끔 하는 규칙과 도구의 집합을 제공한다.
  - 쉽게 이해하려면 Java 진형의 Interface 또한 API라고 볼 수 있다.

### REST?

- REST란 `REpresentational State Transfer`의 약자로 웹 기반의 소프트웨어 아키텍처 스타일 중 하나입니다.
- 인터넷 상의 시스템 간에 상호 운용성을 제공하는 방법이며, 시스템 제각각의 독립적인 진화를 보장하기 위한 방법 중 하나이다
- REST 아키텍처 스타일
  - Client-Server
  - Stateless
  - Cache
  - Uniform Interface
    - Identification of resources
    - manipulation of resources through represenations
    - 💬 self-descrive messages : `해당 아키텍처 스타일을 대부분 무시한다.`
    - 💬 hypermedia as the engine of appliaction state (HATEOAS) : `해당 아키텍처 스타일을 대부분 무시한다.`
  - Layered System
  - Code-On-Demand (optional)

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
    `groovy
dependencies {
   //runtimeOnly 'com.h2database:h2'
   /** 변경  */
   testImplementation 'com.h2database:h2'
}
`
    | 스코프 | Maven 설정 | Gradle 설정 | 설명 |
    |-----------------|--------------------------------------|--------------------------------------------|--------------------------------------------|
    | 컴파일 | `<scope>compile</scope>` | `implementation 'group:artifact:version'` | 모든 빌드 단계에서 사용되는 기본적인 의존성 범위 |
    | 컴파일 전용 | `<scope>provided</scope>` | `compileOnly 'group:artifact:version'` | 컴파일 시에만 사용되며, 런타임에서는 제외됨 |
    | 런타임 전용 | `<scope>runtime</scope>` | `runtimeOnly 'group:artifact:version'` | 런타임 시에만 사용되며, 컴파일 시에는 사용되지 않음 |
    | 시스템 | `<scope>system</scope>` | 사용하지 않음 (사용 시에는 추가 설정 필요) | 시스템에 직접 설치된 JAR 파일과 같은 외부 JAR 파일에 대한 의존성 |
    | 테스트 | `<scope>test</scope>` | `testImplementation 'group:artifact:version'`| 테스트 코드에서만 사용되는 의존성 |
    | 어노테이션 프로세서 | `<scope>compile</scope>` + 어노테이션 프로세서 플러그인 | `annotationProcessor 'group:artifact:version'` | 컴파일 시에만 사용되는 어노테이션 프로세서 |

## Entity

### 생성

- Java Bean 스팩에 맞게 빌더 패턴만 사용하게끔 하지말자
  - `@NoArgsConstructor`를 꼭 달아주자!
    - ex) `new ??()`를 사용하기 위함
- `@Builer`는 매개변수가 없는 디폴트 생성자를 생성해주는 메서드를 만들어 주지 않는다.
  - 따라서 `@NoArgsConstructor`를 사용하라면 `@AllArgsConstructor`는 항상 같이 따라 다닌다 보면 된다.
- `@EqualsAndHashCode`를 사용하면 stackoverflow가 생길 수 있는 일을 미연에 방지가 가능하다.
  - 지정한 값을 기준으로 entity간의 비교가 가능해지기 떄문이다.
  - Set형태로 여러개 지정이가능하다
    - 다른 Entity를 참조하는 필드는 넣지 말자 `상호참조`로 인해 stackoverflow가 발생 할 수 있다.
- `@Data`를 사용하지 않는 이유 또한 위와 같은 이유이다 **EqualsAndHashCode**를 모든 필드로 만들어 버림
- Entity 코드

  ```java
  @Builder // Builder 어노테이션만 사용 시 아무것도 없는 생성자를 만들 수가 없는 문제가 있다.
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  /**
  * 지정한 값을 기준으로 entity간의 비교가 가능해짐
  * - 사용을 하지 않으면 모든 값을 기준으로 비교하는데 이떄 "상호 참조"떄문에 stackoverflow가 발생할 수 도 있음
  * - 원한다먄 Set 형태로도 여러개의 비교 값을 지정이 가능함
  *   - ex) ( of = {"id", "name"})
  * ✨ 여기서도 중요한건  stackoverflow가 발생하지 않게 EqualsAndHashCode에는
  *    다른 Entity를 참조하는 필드를 넣지 않느 것이다.
  * */
  @EqualsAndHashCode( of = "id")
     // @Data  <<가 있지만 Entity에서는 사용하지말자 위에서 말한 EqualsAndHashCode를 모든 필드를 대상으로 만들기 떄문이다.
      public class Event {
      // 식별자
      private Integer id;
      // 오프라인 구분
      private boolean offline;
      // 유료, 무료구분
      private boolean free;
      // 이벤트 상태
      private EventStatus eventStatus;

      private String name;
      private String description;
      private LocalDateTime beginEnrollmentDateTime;
      private LocalDateTime closeEnrollmentDateTime;
      private LocalDateTime beginEventDateTime;
      private LocalDateTime endEventDateTime;
      // (optional) 이게 없으면 온라인 모임
      private String location;
      // (optional)
      private int basePrice;
      // (optional)
      private int maxPrice;
      private int limitOfEnrollment;
      }
  ```

### 테스트 - Entity

- `assertj`를 통해 테스트를 진행 해주자 쉽게 테스트가 가능하다.
  - `import static org.assertj.core.api.Assertions.*`
- Given-When-Then 패턴을 사용하자
  - Given : 테스트에서 구체화하고자 하는 행동을 시작하기 전에 테스트 상태를 설명하는 부분
  - When : 구체화하고자 하는 그 행동
  - Then : 어떤 특정한 행동 때문에 발생할거라고 예상되는 변화에 대한 설명
- 테스트 코드

  ```java
  package com.yoo.restAPI.events;
  import org.junit.jupiter.api.Test;
  import static org.assertj.core.api.Assertions.assertThat;

  class EventTest {

      @Test
      void builder() {
          Event event = Event.builder()
                  .name("Builder Pattern을 사용해서 만듬")
                  .build();
          assertThat(event).isNotNull();
      }

      @Test
      void havaBean() {
          // Given
          String name = "Java Bean  스팩에 맞게 사용 하여 만듬";
          String description = "Spring Boot Project Test Code!";

          // When
          Event event = new Event();
          event.setName(name);
          event.setDescription(description);

          // Then
          assertThat(event.getName()).isEqualTo(name);
          assertThat(event.getDescription()).isEqualTo(description);

      }
  }
  ```

### 테스트 - Contorller

- `@RunWith(SpringRunner.class)`를 통해 Spring 컨택스트를 등록하여 테스트가 가능하다.
  - `import org.junit.runner`를 사용 없다면 알트 + 엔터를 이용해 설치 해주자
- `@WebMvcTest`
  - MockMvc Beab을 자동으로 설정해준다
  - 웹 관련 빈만 등록해 준다(슬라이스)
- `MockMvc mockMvc;`
  - Spring Mvc 테스트에 있어서 가장 **핵심 적인 클래스** 이다.
  - 웹 서버를 **띄우지 않기** 떄문에 빠르다
  - Dispatcherservlet을 만들기 떄문에 단위 테스트보다는 느리다
- 주의
  - `MediaTypes.HAL_JSON`을 사용할때 **"s"**를 잊지말자!!
    - `import org.springframework.hateoas.MediaTypes;`를 사용해 Import 해야 함!!
  - Junit 버전에 맞게 import 해줘야함 안그러면 에러 발생
    - `//import org.junit.Test; ❌ Junit4버전`
    - `import org.junit.jupiter.api.Test; // 👍 Junit5버전`
- 테스트 코드

  ```java
  //import org.junit.Test; ❌ Junit4버전
  import org.junit.jupiter.api.Test; // 👍 Junit5버전
  import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
  import org.springframework.hateoas.MediaTypes;
  import org.springframework.http.MediaType;
  import org.springframework.test.context.junit4.SpringRunner;
  import org.springframework.test.web.servlet.MockMvc;

  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

  @RunWith(SpringRunner.class)    //Spring 테스트 컨텍스트를 관리하면서 테스트를 실행하는 데 사용되는 JUnit 러너입니다.
  @WebMvcTest                     // MockMvc 빈을 자동으로 설정해준다 ___ 웹 관련 빈만 등록해 준다(슬라이스)
  public class EventControllerTests {

      /**
      * - Spring Mvc 테스트에 있어서 가장 핵심 적인 클래스 이다.
      * - 웹서버를 띄우지 않기 떄문에 빠르다
      * - 디스패처서블릿을 만들기 떄문에 단위 테스트보다는 느리다
      * */
      @Autowired
      MockMvc mockMvc;

      @Test
      public void createEvent() throws Exception {
          mockMvc.perform(
                          post("/api/events")
                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                          .accept(MediaTypes.HAL_JSON) // HATOAS를 Import 해줘야 함
                  )
                  .andDo(print())
                  .andExpect(status().isCreated());
      }
  }

  ```

### 테스트 - Contorller // Body 추가

- Spring Boot는 `Jackson`이 자동으로 의존성 주입이 되어있기에 `@Autowired`를 통해 쉽게 사용이 가능하다.
- MockMvc객체에서 Body는 `content()`에 넣는다
- 테스트 코드

  ```java
  @RunWith(SpringRunner.class)
  @WebMvcTest
  public class EventControllerTests {

      @Autowired
      private MockMvc mockMvc;

      // 👉 Spring Boot는 자동으로 Jackson이 의존성주입이 되어이 있음
      @Autowired
      private ObjectMapper objectMapper;

      @Test
      public void createEvent() throws Exception {

          Event event = Event.builder()
                  .name("Spring")
                  .description("Rest API Test")
                  .beginEventDateTime(LocalDateTime.now().minusDays(2))
                  .closeEnrollmentDateTime(LocalDateTime.now())
                  .endEventDateTime(LocalDateTime.now())
                  .basePrice(100)
                  .maxPrice(200)
                  .limitOfEnrollment(100)
                  .location("공릉역")
                  .build();

          mockMvc.perform(
                          post("/api/events")
                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                          .accept(MediaTypes.HAL_JSON)
                          // 💬 Body 값 등록
                          .content(objectMapper.writeValueAsString(event))
                  )
                  .andDo(print())
                  .andExpect(status().isCreated())      // 성공일 경우 201 반환
                  .andExpect(jsonPath("id").exists());  // 응답 값에 id가 있는지 확인
      }
  }
  ```

- Header에 HATEOS 링크를 만들 떄 `linkTo()`를 사용할 떄 Import를 주의하자 - **삽질 1시간넘게 함**
  - `//import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;` ❌ 이거 아님 ...
  - `import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;` 👍 이거로 해야함 ...
- `@RequestMapping`에 consumes , produces 설정을 하면 하위 메서드에 전역적으로 설정된다.
- `linkTo()`, `methodOn()`, `slash()`를 사용하면 링크를 만들 수 있다
- 컨트롤러 코드

  ```java
  // ⭐ WebMvcLinkBuilder를 import 해줘야한다 .. 삽질함..
  import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
  //import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo; ❌ 이거 아님 ...

  @Controller
  // ⭐ RequestMapping을 사용해서 produces를 지정하면 하위 모든 Method의 반환 타입을 지정 가능하다!
  @RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE )
  public class EventController {

      @PostMapping
      public ResponseEntity createEvent(@RequestBody Event event){
          /** Method에 path정보가 있을 경우 "methodOn()" 와 "해당함수명()" 를 사용해 추출해야 했음  */
          // URI createdUri = linkTo(methodOn(EventController.class).createEvent(event)).slash("{id}").toUri();

          URI createdUri = linkTo(EventController.class).slash("{id}").toUri();
          event.setId(999);
          return ResponseEntity.created(createdUri).body(event);
      }

  }
  ```

### 테스트 - 다른 빈 연결

- 이전과 같은 코드에서 테스트 시 해당 Controller가 생성자로 다른 Bean을 주입받고있다면 에러가 발생한다.
  - 에러 코드
    - `Parameter 0 of constructor in com.yoo.restAPI.events.EventController required a bean of type 'com.yoo.restAPI.events.EventRepository' that could not be found.`
  - 해결방법 : `@MockBean`을 활용하면 된다. - 단 해당 방법을 사용해도 해당 요청에 따른 Controller의 응답값은 null이기에 처리다 따로 필요하다.
    - `null point`에러를 처리하기 위해서는 **스터빙이** 필요하다
      - `Mockito.when(레포지토리.save(?)).thenReturn(?);`와 같이 Mockito를 사용하면 스터빙이 가능하다
- 팁
  - `HttpHeaders` 와 `MediaTypes` or `MediaType`을 활용해서 타입 세이프티하게 구현 및 테스트하자!!
- 예시 코드

  ```java
  // Controller
  @RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE )
  public class EventController {
      private final EventRepository eventRepository;
      @PostMapping
      public ResponseEntity createEvent(@RequestBody Event event){
          // 저장
          Event newEvent =  this.eventRepository.save(event);
          // code ...
      }
  }

  // Test Code
  @RunWith(SpringRunner.class)
  @WebMvcTest
  public class EventControllerTests {

      @Autowired
      private MockMvc mockMvc;

      @Autowired
      private ObjectMapper objectMapper;

      // ⭐ @MockBean을 통해 가짜 객체 생성
      @MockBean
      private EventRepository eventRepository;

      @Test
      public void createEvent() throws Exception {

        /**
         * 👉 스터빙 코드
         *    - 사용하지 않을 시 저장해도 null을 반환하기에 저장 시 진행 될 코드를 만드는것
         *    - Id를 지정해주는 것은 시퀀스로 자동 생성으로 할 것이기에 body 값에 없기 떄문임!
         * */
        event.setId(999);
        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(/* 생략 */)
                /** Then */
                .andExpect(status().isCreated())                  // 성공일 경우 201 반환
                .andExpect(jsonPath("id").exists())               // 응답 값에 id가 있는지 확인
                .andExpect(header().exists(HttpHeaders.LOCATION)) // 응답 로케이션 유무 확인
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE)); // Content-Type 체크
      }
  }
  ```

### ModelMapper

- `DTO -> Entity` 혹은 `Entity -> DTO`와 같은 변환이 필요할 떄 유용한 **라이브러리**이다
- DTO 사용 이유 ?
  - 받은 값을 제한할 수 있다.
  - 구현 시 Entity는 최대한 건들지 않는 것이 중요하다
    - 이유로는 여러가지가 있겠지만 무결성을 유지하기 위해서이다 Entity를 수정하다보면 추후 개발이 어려워짐
  - validation 체크 시에도 Entity만 사용하다 보면 너무 과도하게 어노테이션이 집중되어 클린한 코드가 되지 않기도 함
- 단점

  - 직접 코드를 입력하는 것보다는 속도는 느리나 Java 버전이 올라가면서 빨라짐.
  - 세세한 컨트롤은 힘들다. 정말 받은 값을 변환하여 넣어주는 역할만 해준다.

- 사용 방법

  - 의존성 추가
    - `implementation group: 'org.modelmapper', name: 'modelmapper', version: '3.2.0'`
  - Appliction에 Bean등록

    ```java
    @SpringBootApplication
    public class RestApiApplication {

      // Application Start Code 생략 ...

      /** Dependencies에 추가한 ModelMapper를 빈으로 추가 */
      @Bean
      public ModelMapper modelMapper(){ return new ModelMapper(); }

    }
    ```

  - 사용
    - `Event event = modelMapper.map(eventDTO, Event.class);`

### ModelMapper - 사용 시 TestCode 문제

- 기존에 사용하던 테스트 코드에서 받아온 값이 `null`로 들어온다.
  - 분명 Given 단계에서 생성 및 `Event event = Event.builder()...` 모킹을 통해 주입 중이다. `Mockito.when(eventRepository.save(event)).thenReturn(event);`
- 이유
- 테스트 코드의 모킹 코드를 보면 해답이 나온다

  - save 시 Event객체를 통해 만들어진 값을 save 후 반환하는데
  - Controller에서 보면 새로 만든 객체를 주입 한다

    - Contoller 코드

      ```java
      public class EventController {

          // Application에서 Bean등록 완료
          private final ModelMapper modelMapper;

          @PostMapping
          public ResponseEntity createEvent(@RequestBody EventDTO eventDTO){
              // ✨ 포인트!! 새로 객채를 만들고 있음
              // 👉 modelMapper를 통해 DTO -> Entity 시킴
              Event event = modelMapper.map(eventDTO, Event.class);
              // 저장 - 새로 만든 객체임!!!
              Event newEvent =  this.eventRepository.save(event);
          }
      }
      ```

  - 이러한 이유로 모킹과 다른 객채로 인식하여 스터빙이 적용이 안된 것이다.
    - 그렇기에 null을 반환하고 null point Exception이 발생한것이다

- 해결 방법 ??
  - 스터빙을 안해버리면된다!
  - `@SpringBootTest,@AutoConfigureMockMvc`를 통해 스터빙을 끊고 자동으로 전체 주입받은 빈을 사용하면 된다.
- 위와 같은 방법에 관해서 테스트 코드를 하는데 전체 빈을 스캔하는게 맞을까라는 고민을 했었는데
  - 강의에서는 웹쪽 관련 테스트 코드는 해당 방법일 선호하며 추천한한다.
    - 웹 테스트 시 많은 모킹이 필요여 번거롭고 관리 또한 힘들다
      - 코드가 바뀌면 꺠질 위험도가 올라가는 문제 또한 있음
    - 시간이 많이 들며 그럴 바에는 차라리 전체 빈을 주입받아 테스트하는 것이 효율적이라 하였다.
- `@SpringBootTest`를 사용하면 Application에 설정되어있는 모든 빈을 주입하여 테스트하며 실제 어플리케이션과 가장 가까운 형태로 테스트가 가능하다.
  - API 테스트 시 슬라이싱 테스트 보다 해당 방법을 선호한다.
  <hr/>

## Unkown Data 처리 방법

- `appilcation.properties` 설정을 통해 데이터가 Json -> DTO 변경 시 없는 데이터라면 BadRequest를 반환한다.
- 사용 방법
  - `spring.jackson.deserialization.fail-on-unknown-properties=true`
- 해당 설정은 deserialization 뿐만 아니라 serialization 설정도 할 수 있으니 참고하자.
- 좀 더 타이트하게 API에 제한을 두고 싶을 때 사용하자 사용 유무는 권장이 아닌 개발 상황에 맞춰서 해주면 된다.
- 용어설명
  - `deserialization` : JSON 문자열 -> Java의 DTO Class로 매핑할 경우
  - `serialization` : Java의 DTO Class -> JSON 문자열로 매핑할 경우

## 입력값이 이상한 경우 처리 방법

- `implementation 'org.springframework.boot:spring-boot-starter-validation'`를 디펜던시로 추가해 주자
- Null 또는 Empty, Min, Max 처리 방법

  - Controller 파라미터 내 `@Valid`를 사용해서 감시 대상으로 지정
  - DTO 내부 변수 상단 `@NotEmpty`, `@NotNull`, `@Min(0)`, `@Max(0)` 어노테이션 지정을 통해 검증
  - 해당 검증에 적합하지 못 할 시 `Errors`에 에러가 발생하여 돌아온다.
  - `errors.hasErrors()`를 통해 예외 처리

  ```java
    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors){
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.created(createdUri).body(event);
    }
  ```

- Validation 자체를 확인
- 검증을 처리할 Class를 생성 후 `@Component`를 통해 Spring Bean에 등록
- 메서드를 생성하여 검증 로직 작성 후 조건에 맞지 않을 시 `erros`에 에러 주입

  - errors.rejectValue(필드명, 에러코드, 에러 메세지);

- 사용 예시

  - 검증 핸들러

    ```java
    @Component // Bean 등록
    public class EventValidator {
        public void validate(EventDTO eventDTO, Errors errors){
            if(eventDTO.getBasePrice() > eventDTO.getMaxPrice()
                && eventDTO.getMaxPrice() > 0 ){
                  // 👉 rejectValue()를 통해 에러 주입
                errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
                errors.rejectValue("maxPrice", "wrongValue", "MaxPrice is wrong");
            }

            LocalDateTime eventEndTime =  eventDTO.getEndEventDateTime();
            if(eventEndTime.isBefore(eventDTO.getBeginEventDateTime())){
                errors.rejectValue("endEventDateTime", "wrongValue", " endEventDateTime is wrong");
            }

            // TODO 이런식이르 검증 로직을 만들어서 errors를 reject해준다.
        }
    }
    ```

- 사용 컨트롤러

  ```java
  @Controller
  @RequiredArgsConstructor
  @RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE )
  public class EventController {

      private final EventValidator eventValidator;

      @PostMapping
      public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors){
          // 👉 검증 핸들러 적용
          eventValidator.validate(eventDTO, errors);

          if(errors.hasErrors()){
              return ResponseEntity.badRequest().build();
          }

          return ResponseEntity.created(createdUri).body(event);
      }

  }
  ```

## Paramter를 받아 Test코드 작성

- 이전 작성했던 테스트 코드를 보면 불필요하게 반복되는 코드가 있는걸 발견 할 수있다.

  ```java
  @Test
    void testFree() {
        // Given
        Event event = Event.builder()
                .basePrice(0)
                .maxPrice(0)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isTrue();

        /**************/

        // Given
        event = Event.builder()
                .basePrice(1000)
                .maxPrice(0)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isFalse();
    }
  ```

- 위의 해당 코드는 Given 값과 Then 결과를 제외하고는 전부 같은 코드인것을 확인 할 수 있다.
- 이러한 반복 코드는 파리미터를 받아와 처리가 가능하다.
- JUnit 버전별 차이점
- JUnit4를 사용할 경우 `JUnitParams`를 디펜던시에 추가하여 처리가 가능하나 대부분의 추세는 JUnit5를 사용하므로 제외한다.
- JUnit5를 사용할 경우 추가적인 디펜던시 없이 처리가 가능하다.
- 사용 방법
  - 필수
    - `@ParameterizedTest`를 이용하면 여러 개의 테스트 케이스를 사용할 수 있다.
  - 이후 선택
    - `@ValueSource`: 한 개의 인수 입력 시 사용
    - `@CsvSource`: 한 개의 인수와 해당 인수를 넣었을 때의 결과값 입력 시 사용
    - `@NullSource`, `@EmptySource`, `@NullOrEmptySource`: null 또는 공백값에 대한 테스트 시 사용
    - `@EnumSource`: Enum 값에 대한 테스트 시 사용
    - `@MethodSource`: 테스트에 복잡한 인수를 제공하고자 할 때 메소드를 만들어서 사용
- 개인적인 생각
  - Type Safety를 위해서 `@MethodSource`위주로 사용하자
- 예시

  ```java
  // 👉 해당 Stream의 순서대로 값이 들어간다.
  private static Stream<Arguments> provideFree() {
      return Stream.of( // 횟수
              // 각각의 argument는 순서대로 테스트의 파라미터 값
              Arguments.of(0, 0, true),
              Arguments.of(1_000, 0, true)
      );
  }

  @DisplayName("파리머터를 통해 여러번 테스트가 가능")
  @ParameterizedTest //👉 해당 어노테이션을 사용하면 여러개의 테스트 케이스를 한번에 실행 가능
  @MethodSource("provideFree")  // ✨ 들어갈 메서드명 주입
  public void testFree_Refactoring(int basePrice, int maxPrice, boolean expected) {
      // Given
      Event event = Event.builder()
              .basePrice(basePrice)
              .maxPrice(maxPrice)
              .build();

      // When
      event.update();

      // Then
      assertThat(event.isFree()).isEqualTo(expected);

  }
  ```

## 스프링 HATEOAS

- Docs
  - https://docs.spring.io/spring-hateoas/docs/current/reference/html/
- 기능
  - 링크 만드는 기능
    - 문자열 가지고 만들기
    - 컨트롤러와 메소드로 만들기
    - ex) `WebMvcLinkBuilder.linkTo()` and `WebMvcLinkBuilder.methodOn()` 사용
  - 리소스 만드는 기능
    - 리소스: 데이터 + 링크
  - 링크 찾아주는 기능
    - Traverson
    - LinkDiscoverers
- 링크
  - HREF (hypertext reference) : 링크 주소
  - REL (Relation) : rel 속성은 현재 문서와 외부 리소스 사이의 연관 관계를 명시합니다.
    - self
    - profile
    - update-event
    - query-events

### EntityModel을 사용하여 `_link` 추가

- \_Link를 생성 할 수 있는 HATEOAS 컨테이너 객체 생성하는 기능을 제공한다.
- 이전 2.X 초반 SpringBoot에서는 따로 Class 생성 후 Resourse<T>를 상속 받아 구현 하였지만 버전이 바뀌면서 변경되었다.
  - ex) `public class EventResource extends Resource<Event>` 이후 super를 통한 생성자 필요
- `EntityModel<T>` 사용 방법

  ```java
  @PostMapping
  public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors){

      // 저장
      Event newEvent =  this.eventRepository.save(event);

      // 💬 EntityModel<대상>을 통해 _Link를 생성 할 수 있는 HATEOAS 컨테이너 객체 생성
      EntityModel<Event> eventEntityModel = EntityModel.of(newEvent);

      // 👉 현재 사용하는 Class의 주소 정보를 읽어서 가져옴
      WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
      URI createdUri = selfLinkBuilder.toUri();

      // ⭐️ HATEOAS 컨테이너 객체 내 add이벤트를 통해 _link 생성
      /** _link.query-events */
      eventEntityModel.add(linkTo(EventController.class).withRel("query-events"));
      /** _link.self */
      eventEntityModel.add(selfLinkBuilder.withSelfRel());   // 👉 withSelfRel()를 사용해서 자기 자신 사용
      /** _link.self */
      eventEntityModel.add(selfLinkBuilder.withRel("update-event"));

      // ❌ eventEntityModel.add(Link.of("http://localhost:8080/??")); linkTo()를 사용하자 이건 타입 세이프티하지 않음!!

      // 👉 created()에 들어가면 header에 생성된다.
      // Headers = [Location:"http://localhost/api/events/1", Content-Type:"application/hal+json"]
      return ResponseEntity.created(createdUri).body(eventEntityModel);
  }
  ```

## Rest Docs

- 테스트 코드 기반으로 Restful API 문서를 돕는 라이브라리이다.
- API Spec과 문서화를 위한 테스트 코드가 일치하지 않으면 테스트 빌드를 실패하게 되어 테스트 코드로 검증된 문서를 보장할 수 있습니다.
- Swagger 또한 가능한 기능인데 왜 써야할까?

  - Swagger는 API를 테스트해 볼 수 있는 화면을 제공하여 동작 테스트하는 용도에 조금 더 특화되어있습니다.
  - 테스트 기반이 아니기에 문서가 100% 정확하다고 확신할 수 없다.
  - 로직에 애노테이션을 통해 명세를 작성하게 되는데 지속적으로 사용하게 된다면 명세를 위한 코드들이 많이 붙게되어 전체적으로 가독성이 떨어진다.
  - Swagger 사용 시 예시

    ```java
    public class SignupForm {
      @ApiModelProperty(value = "카카오 id", required = true, example = "1")
      private Long id;

      @ApiModelProperty(value = "카카오 image url", required = true, example = "\"http://k.kakaocdn.net\"")
      private String imageFileUrl;
    }
    ```

- 사용 방법

  - 테스트 코드에 작성해야한다.
  - 테스트가 성공해야지만 문서가 만들어진다.
  - 문서의 생성 위치는 따로 설정하지 않았다면 `build -> generated-snippets -> 설정한 doucument명` 위치에 생성된다.
  - `@AutoConfigureRestDocs` 어노테이션을 테스트 class 최상단에 달아줘야한다.

  ```java
  import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
  import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;

  @SpringBootTest
  @AutoConfigureMockMvc
  @AutoConfigureRestDocs // ✨ 1. snippset을 만들 것이라는 어노테이션 지정
  public class EventControllerTests {
    @Autowired
    private MockMvc mockMvc;  // ✨ 2. mockMvc기반 테스트로 만들어지므로 지정

    @Test
    @DisplayName("HAL_JSON 체크")
    public void createEvent_HATEOAS() throws Exception {
        /** Given */
        EventDTO event = EventDTO.builder().build();

        /** When */
        mockMvc.perform(
                        post("/api/events")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(event))
                )
                .andDo(print())
                /** Then */
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                // ✨ 3. document()로 이름을 지정하여 Rest Docs 생성
                .andDo(document("create-event"))

        ;
    }
  }
  ```

### RestDocsMockMvcConfigurationCustomizer 적용

- 위의 상태로 만들어진 doc내용을 보면 보기 힘든 형태로 되어있는것을 볼 수 있다.
  - 이러한 문서를 해당 클래스를 사용해서 커스텀하면 내가 원하는 형태로 변경이 가능하다.
- 사용 방법

  - test 디렉토리 내부 common 디렉토리 생성 후 class 파일 생성
  - `@TestConfiguration` 테스트용 설정 어노테이션 추가
  - RestDocsMockMvcConfigurationCustomizer 클래스 생성 후 `@Bean`등록

    ```java
    package com.yoo.restAPI.common;

    import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.context.annotation.Bean;

    import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

    @TestConfiguration // ✨ 테스트용 설정
    public class RestDocsConfiguration {
        @Bean // ✨ Bean Factory 스캔 대상 추가
        public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcBuilderCustomizer(){
            return config -> config.operationPreprocessors()
                    // ✏️ prettyPrint() 살정을 통해 요청, 응답 커스텀
                    .withResponseDefaults(prettyPrint())
                    .withRequestDefaults(prettyPrint());
        }
    }
    ```

  - 실제 테스트 부분에 `@Import(대상)`를 사용해서 지정
    ```java
    @SpringBootTest
    @AutoConfigureMockMvc
    @AutoConfigureRestDocs
    @Import(RestDocsConfiguration.class)    // 💬 만들어준 RestDocsConfiguration를 등록 사용
    public class EventControllerTests {
        // Test Code ...
    }
    ```

### Snippet 필요 생성

- `org.springframework.restdocs.~~` 클래스 내 메서드를 사용하여 만들 수 있다.
- `document("지정폴더명"), ~~` 뒤 이이서 원하는 adocs를 만들 수 있다.
- 각각의 메서드마다 생성되는 파일 및 목적이 다르다. - 각각 모두 테스트가 완료 돼야 생성된다.
  - `links()` : Link에 관련된 문서 조각이 생성된다. (HAL_JSON)에서 생성되는 `_links:` 관련 데이터를 체크 및 생성
  - `requestHeaders()` 요청 Header에 대한 문서 조각 생성
  - `requestFields()` 요청 Body에 필요한 필드 값들 조각 생성
  - `responseHeaders()` 응답 Header 생성
  - `responseFields()` 응답 Body 생성
- 사용 방법

  ```java
  @Test
  @DisplayName("문서 조각 생성")
  public void createEvent_HATEOAS() throws Exception {
      /** Given */
      EventDTO event = EventDTO.builder()
              .name("Spring")
              .description("Rest API Test")
              .beginEnrollmentDateTime(LocalDateTime.now())
              .closeEnrollmentDateTime(LocalDateTime.now().plusDays(10))
              .beginEventDateTime(LocalDateTime.now())
              .endEventDateTime(LocalDateTime.now().plusDays(10))
              .basePrice(100)
              .maxPrice(200)
              .limitOfEnrollment(100)
              .location("공릉역")
              .build();

      /** When */
      mockMvc.perform(
                      post("/api/events")
                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                              .accept(MediaTypes.HAL_JSON)
                              .content(objectMapper.writeValueAsString(event))
              )
              .andDo(print())
              /** Then */
              .andExpect(status().isCreated())
              .andExpect(jsonPath("id").exists())
              .andExpect(header().exists(HttpHeaders.LOCATION))
              .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
              .andExpect(jsonPath("id").value(Matchers.not(100)))
              .andExpect(jsonPath("free").value(Matchers.not(true)))
              // 👉 Link를 가지는지 체크
              .andExpect(jsonPath("_links.self").exists())
              .andExpect(jsonPath("_links.query-events").exists())
              .andExpect(jsonPath("_links.update-event").exists())
              // ✏️ Rest Docs 생성
              .andDo(document("create-event",
                      // 👉 Link에 관련된 문서 조각이 생성된다!! Document만 사용하면 생성되지 않음  :: links.adoc 파일이 생성됨
                      links(
                              linkWithRel("self").description("link to self")
                              , linkWithRel("query-events").description("link to query-events")
                              , linkWithRel("update-event").description("link to update-event")
                      ),
                      // 👉 Header 관련 문서 조각 생성 :: request-headers.adoc 생성
                      requestHeaders(
                              headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                              , headerWithName(HttpHeaders.ACCEPT).description("accept")
                      ),
                      // 👉 요청에 필요한 필드목록 :: request-fields.adoc 생성
                      requestFields(
                              PayloadDocumentation.fieldWithPath("name").description("Name fof new Event")
                              , PayloadDocumentation.fieldWithPath("description").description("description of new Event")
                              , PayloadDocumentation.fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new Event")
                              , PayloadDocumentation.fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new Event")
                              , PayloadDocumentation.fieldWithPath("beginEventDateTime").description("beginEventDateTime of new Event")
                              , PayloadDocumentation.fieldWithPath("endEventDateTime").description("endEventDateTime of new Event")
                              , PayloadDocumentation.fieldWithPath("location").description("location of new Event")
                              , PayloadDocumentation.fieldWithPath("basePrice").description("basePrice of new Event")
                              , PayloadDocumentation.fieldWithPath("maxPrice").description("maxPrice of new Event")
                              , PayloadDocumentation.fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new Event")
                      ),
                      // ✍️ 응답 Header 문서 조각 생성 :: response-header.adoc 생성
                      responseHeaders(
                              headerWithName(HttpHeaders.LOCATION).description("Location Header")
                              , headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                      ) ,
                      // ✍️ 응답 field 문서 조각 :: response-field.adoc 생성
                      responseFields(  // ✨ 모든 필드를 검증하여 문서화 하고싶을 경우 사용 :: 현재 link부분을 또한번  추가하지 않으면 에러 발생 .. 왜지 ..links()에서 검사하는데..
                      // 👎 relaxedResponseFields( // ✏️ 아래 작성한 필드들만 문서로 만들어줌  <<< 단점으로는 정확한 문서화가 되지 않음 , 장점 문서 일부분만 테스트가 가능하다 :: 비추천!! 확살하지 않아짐
                              PayloadDocumentation.fieldWithPath("id").description("New Id")
                              , PayloadDocumentation.fieldWithPath("name").description("Name fof new Event")
                              , PayloadDocumentation.fieldWithPath("description").description("description of new Event")
                              , PayloadDocumentation.fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new Event")
                              , PayloadDocumentation.fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new Event")
                              , PayloadDocumentation.fieldWithPath("beginEventDateTime").description("beginEventDateTime of new Event")
                              , PayloadDocumentation.fieldWithPath("endEventDateTime").description("endEventDateTime of new Event")
                              , PayloadDocumentation.fieldWithPath("location").description("location of new Event")
                              , PayloadDocumentation.fieldWithPath("basePrice").description("basePrice of new Event")
                              , PayloadDocumentation.fieldWithPath("maxPrice").description("maxPrice of new Event")
                              , PayloadDocumentation.fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new Event")
                              , PayloadDocumentation.fieldWithPath("free").description("is it free??")
                              , PayloadDocumentation.fieldWithPath("offline").description("is it offline??")
                              , PayloadDocumentation.fieldWithPath("eventStatus").description("event status")
                              , PayloadDocumentation.fieldWithPath("_links.self.href").description("self!!! 왜필요한지 모르겠다 위에서 links() 검사하는데 ..")
                              , PayloadDocumentation.fieldWithPath("_links.update-event.href").description("update-event!!! 왜필요한지 모르겠다 위에서 links() 검사하는데 ..")
                              , PayloadDocumentation.fieldWithPath("_links.query-events.href").description("query-events!!! 왜필요한지 모르겠다 위에서 links() 검사하는데 ..")
                      )

              ))

      ;
  }
  ```

## AscIIdocs

- 설정 방법
  - build.gralde 수정 필요
    - `configurations` 내 asciidoctorExtensions 추가
    - `tasks.named('asciidoctor')` 태스크 추가
    - 생성 jar 명령어 추가 `bootJar `
- 바탕이 될 doc를 추가해준다
  - 경로 : `src -> docs[추가] -> asciidoc[추가] -> index.adoc`
    - 기본 틀이될 형식은 만들어진거 복사해서 사용하자 - [링크](https://github.com/edel1212/restAPIStudy/blob/main/src/docs/asciidoc/index.adoc)
- 이후 build를 실행하면 `resources->static->docs->index.html`로 변환 생성된다.
- 주의 할점
  - Test 코드 중 실패하는 코드가 있을 경우 에러를 반환하므로 주의하자!

```gralde
plugins {
	id 'java'
	id 'org.springframework.boot' version '3.1.7'
	id 'io.spring.dependency-management' version '1.1.4'
	id 'org.asciidoctor.jvm.convert' version '3.3.2'
}

group = 'com.yoo'
version = '0.0.1-SNAPSHOT'

java {
	sourceCompatibility = '17'
}

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
	// ✨ asciidoc 생성을 위해 추가
	asciidoctorExtensions
}

repositories {
	mavenCentral()
}

ext {
	set('snippetsDir', file("build/generated-snippets"))
}

dependencies {
	// ✨ asciidoc 생성을 위해 추가
	asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor'
}

tasks.named('test') {
	outputs.dir snippetsDir
	useJUnitPlatform()
}

tasks.named('asciidoctor') {
	inputs.dir snippetsDir
	// ✨ asciidoc 생성을 위해 추가
	configurations 'asciidoctorExtensions'
	dependsOn test
}

// ✨ asciidoc 생성을 위해 추가
bootJar {
	dependsOn asciidoctor

	// build 경로 안에 있는 index.html을 밖으로 꺼내준다.
	copy {
		from "${asciidoctor.outputDir}"
		into "src/main/resources/static/docs"   // src/main/resources/static/docs로 복사
	}

}
```

### 프로필 생성 방법

- 간단하다 위에서 만들어진 asciidoc를 연동만 해주면 해결이다.
- 코드
  - Controller
    ```java
    // ✏️ 프로필 추가! asciidocs 를 통해 생성된 html파일 링크임!
    eventEntityModel.add(Link.of("/docs/index.html#resources-events-create").withRel("profile"));
    ```

## DB 분리하기

- 설정에서 중복된 부분은 같이 사용하고 필요한 부분은 test용에 맞춰 사용
- 실사용 DB : `postgres`, 테스트 DB : `h2` 설정
- 방법
  - h2 DB를 test scope로 지정
    - `testImplementation 'com.h2database:h2'`
  - `application-test.properties` 생성 profile 지정이 가능하게 끔 "-"를 사용해 생성해 주자
  - 테스트 코드내 `@ActiveProfiles("대상")` 지정을 통해 사용하자
- 코드

  - build.gradle
    ```groovy
    dependencies {
      // ✨ Test scope로 변경
      testImplementation 'com.h2database:h2'
    }
    ```
  - application-test.porperties - test용 설정

    ```properties
    spring.datasource.username=sa
    spring.datasource.password=
    spring.datasource.url=jdbc:h2:mem:testdb
    spring.datasource.driver-class-name=org.h2.Driver

    spring.datasource.hikari.jdbc-url=jdbc:h2:mem:testdb

    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
    ```

  - 테스트 코드

    ```java
    @SpringBootTest
    @AutoConfigureMockMvc
    @AutoConfigureRestDocs
    @Import(RestDocsConfiguration.class)
    @ActiveProfiles("test") // 👌 application-test.porperties 지정
    public class EventControllerTests {

    }
    ```

## Index 응답 만들기

- 첫 목록 페이지라 생각하자
- 주의사항
  - `RepresentationModel`객체를 생성하여 사용하자
    - `RepresentationModel.of()`로 반환할 경우 원하던 응답이 나오지 않음 ❌
      - 예상했던 값 : `Body = {"_links":{"events":{"href":"http://localhost:8080/api/events"}}}`
      - 하지만 .. : `Body = {"href":"http://localhost:8080/api/events"}`
  - `linkTo()`메서드는 꼭 ...WebMvcLinkBuilder를 import해서 사용하자
    - WebFlux를 사용할 경우
      - `Handler dispatch failed: java.lang.NoClassDefFoundError: reactor/util/context/ContextView` 에러 발생 주의! 👎
- 사용 예시

  ```java
  @RestController
  public class IndexController {

      @GetMapping("/api")
      public RepresentationModel index(){
          // 👉 var는 임시 변수로 java 10부터 사용이 가능하다
          var index = new RepresentationModel();
          // ✨ linkTo Import 주의!!
          index.add(linkTo(EventController.class).withRel("events"));
          return index;
      }
  }
  ```

### Error 발생 시 처리

- Index페이지를 만든 이유는 바로 Error를 처리하기 위함이 크다.
  - 에러가 발생하면 전체 API사용 목록을 볼 수 있게 끔 보여주기 위함이다.
- 이슈 사항
  - 최초에 작성했던 `JsonSerializer`를 이용한 Errors를 Json화 하는 부분에서 해당 만들어 진 객체를 `EntityModel`로 변환하는
    이슈가 발생
    - Custom DTO를 만들어서 Error를 컨트롤함.
  - ⭐️ 따로 필요 없이 받아온 Error 예외들을 `getFieldErrors()` 혹은 `getFieldError()`를 통해 처리 해주자
- 예시

  ```java
  @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors){
        //  값 검증
        eventValidator.validate(eventDTO, errors);
        //  👉 예외 발생 시 처리
        if(errors.hasErrors()){
            ErrorDTO errorDTO = ErrorDTO.builder()
                    .field("fileName").status(999).message("Error!!").build();
            EntityModel<ErrorDTO> errorModel = EntityModel.of(errorDTO);
            errorModel.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
            return ResponseEntity.badRequest().body(errorModel);
        } // if

        Event event = modelMapper.map(eventDTO, Event.class);
        event.update();
        // 저장
        Event newEvent =  this.eventRepository.save(event);
        EntityModel<Event> eventEntityModel = EntityModel.of(newEvent);
        return ResponseEntity.created(createdUri).body(eventEntityModel);
    }
  ```

## 목록 페이지 (페이징)

- 현재 기본적인 페이징만을 사용하기에 구현 사항에따라 변경이 필요함
  - where 조건이 없음
  - sort 조건이 한정적임
- `PagedResourcesAssembler`을 사용해서 목록뿐만아닌 페이징 정보 또한 추가 가능함
  - 파라미터로 Pageable과 함수를 주입해야한다!
    - 그렇지 않으면 반환 시 페이징 정보만 들어가고 본문 내용이 빠져있는 이슈가 있음
- 예시
  ```java
  @GetMapping
  public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler assembler){
      // 💬 페이지만 반환할 경우 목록만 표출
      Page<Event> page = this.eventRepository.findAll(pageable);
      // ⭐️ PagedResourcesAssembler를 사용하면 시작, 끝 등 _link정보가 자동으로 주입된다.
      var pagedResources  = assembler.toModel(page,entity ->{
          EntityModel<Event> entityModel = EntityModel.of((Event) entity);
          entityModel.add(linkTo(EventController.class).slash(((Event) entity).getId()).withSelfRel());
          return entityModel;
      } );
      // 💬 profile 추가
      pagedResources.add(Link.of("/docs/index.html#resources-query-events").withRel("profile"));
      return ResponseEntity.ok(pagedResources);
  }
  ```

## 내부문자열을 외부설정으로 빼기
- 코드 내 작성되어 있던 문자열을 `properties`로 빼낸 후 Class에 매핑하여 사용
  - 가독성이 좋아지며 한번에 관리하기도 좋아진다. 

- ### dependencies
  ```groovy
  dependencies {
      // application 설정을 Java에 매핑
      annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
  }
  ```
- ### MappingClass
  - `prefix` 설정을 하면 시작명으로 잡을 수 있다.
  ```java
  @Component
  @ConfigurationProperties("my-app")
  @Getter
  @Setter
  public class AppProperties {
      @NotNull
      private String username;
      @NotNull
      private String password;
  }
  ```

- ### application.properties
  ```properties
  my-app.username = "edel1212"
  my-app.password = "123"
  ```

- ### 테스트
  ```java
  @SpringBootTest
  public class AppPropertiesTests {
  
      @Autowired
      private AppProperties properties;
  
      @Test
      void name() {
          System.out.println("userName  :: " + properties.getUsername());
          System.out.println("userPassword  :: " + properties.getPassword());
      }
  }
  ```

## 유용한 intellij 단축키

- `커맨드 + 쉬프트 + t` : 사용 클래스에서의 테스트 코드 생성 및 이동이 가능함
- `컨트롤 + 옵션 + o` : 사용하지 않는 import 자동 삭제
- `알트 + 엔터` : 자동 import
- `알트 + 커맨드 + v` : 중복 값을 추출하여 변수로 만들어 줌
- `커맨드 + 쉬프트 + v` : 과거 복사 이력 사용 가능 윈도우 클립 보드 기능 유사
- `커멘드 + p` : 들어갈 파라미터의 필드가 무엇인지 알려줌

## 유용한 어노테이션

- `@JsonUnwrapped`를 사용하면 직렬화(Object Mapper가 Serializer 시) 해당 부분을 **꺼낸 후** JSON으로 만듬
- 직렬화를 할때 Java Bean스팩에 맞게 직렬화를 한다.



## 개발 시 참고 사항
- 테스트 코드 작성 시 `H2-Base`를 사용한 `In-Memory DB`를 사용한다 해도 다건의 테스트의 경우 데이터가 공유 된다.
  - 해당 이슈를 해결 하기 위해서는 만약 어쩔 수 없는 공유된 데이터의 경우 `@Before`어노테이션을 활용해서 지워주자
    - Ex) 인증 정보 추가 시 계정이 계속해서 같은 ID가 추가되는 문제가 발생
  - 해당 문제는 `@Before`을 사용하지 않더라도 중복 요청이 발생하지 않도록 로직 내에서 처리하는 방식으로도 가능하다.
- `MockMvc`테스트에서 결과값을 받아서 Return 받기
  - `ResultActions`로 해당 테스트의 `perform()`결과를 받아올 수 있다.
    - `perform.andRedurn().getResponse().getContentAsString()`으로 문자열로 반환
      - `var parser = new Jackson2JsonParser();` 객체 생성
        - `parser.parseMap(변환 문자열).get(키 값).toString()`으로 변환이 가능하다
- SpringSecurity에서 승인된 요청에서 인증 보를 받는 방법
  - 1 . 코드 내 직접 `SecurityContextHolder`에 접근하여 받아온다
    - `SecurityContextHolder.getContext().getAuthentication();`을 통해 받아올 수 있다.
  - 2 . SpringSecurity에서 제공하는 어노테이션을 통해 받아온다.
    - `@AuthenticationPrincipal User상속Class 객체`
      - 해당 방법이 객체를 바로 주입 받을 수 있어 코드가 더 간결해 진다.
    - `@AuthenticationPrincipal(experssion = "대상") 대상 객체`
      - 위와 같이 `experssion`를 활용하면 코드가 더욱 간결해 진다.
    - AnnotationClass를 사용해서 더욱 간소화 가능
      - `@AuthenticationPrincipal`는 메타 어노테이션을 지원하므로 간소화 가능
      ```java
      @Target(ElementType.PARAMETER)      // 파라미터 형태로 사용 명시
      @Retention(RetentionPolicy.RUNTIME) // 언제까지 해당 어노테이션 지정 여부 : 런타임
      // @AuthenticationPrincipal(expression = "targetKey")
      /**
      * ✅ 접근 대상이 anonymousUser 권한이 들어올 경우 User 객체를 타고 넘어오지 않아 응답 값이
      *    anonymousUser라는 문자열로 들어오기에 해당 expression의 유연함을 활용해서 적용해주자
      * */
      @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : targetKey")
      public @interface CurrentUser { }
      ```
    - `@AuthenticationPrincipal(experssion = "대상") 대상 객체` -> `@CurrentUser 대상 객체` 간소화
- Log 컬러 추가
  ```properties
  spring.output.ansi.enabled=always
  ```
- P6psy
    - SpringBoot 3.x.x 버전 이상부터는 적용 Dependecies 버전이 올라감
    	-   `implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'`
- 테스트 코드 응답 값 한글 처리
  - 테스트 코드 작성 후 응답 결과가 한글이 깨지는 이슈 발생
  - ctx를 주입하여 한글 설정을 해서 해결하는 듯 했으나 Security의 경우 `apply(security())`를 통해 이슈를 제거했으나 `@AutoConfigureRestDocs`에서 이슈 발생
  	- Error Log :  `REST Docs configuration not found. Did you forget to apply a MockMvcRestDocumentationConfigurer when building the MockMvc instance?`
   - ℹ️ 해결 방법
   	-  application-properties에서 설정으로 해결
		  ```properties
		server:
		  # 한글 반환 처리
		  servlet:
		    encoding:
		      force-response: true
		  ```
