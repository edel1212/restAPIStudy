# Efficient Test

## 방법 및 구조 - 일반적인 로직 테스트

- `assertj`를 사용하면 쉽게 테스트가 가능하다.
    - 주입  : `import static org.assertj.core.api.Assertions.*`
- Given-When-Then 패턴을 사용하자
    - Given: 테스트에서 구체화하고자 하는 행동을 시작하기 전에 테스트 상태를 설명하는 부분
    - When : 구체화하고자 하는 그 행동
    - Then : 어떤 특정한 행동 때문에 발생할거라고 예상되는 변화에 대한 설명
- 사용 예시

  ```java
  class EventTest {
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

## Controller 테스트

- `@RunWith(SpringRunner.class)`를 통해 Spring 컨택스트를 등록하여 테스트가 가능하다.
    - `import org.junit.runner`를 사용 없다면 `알트 + 엔터`를 이용해 설치 해주자
- `@WebMvcTest`
    - MockMvc Beab을 자동으로 설정해준다
    - 웹 관련 `Bean`만 등록해 준다**(슬라이스 방법이라고도 한다.)**
- `MockMvc mockMvc;`
    - Spring Mvc 테스트에 있어서 가장 **핵심 적인 클래스** 이다.
    - 웹 서버를 **띄우지 않기** 떄문에 빠르다
    - Dispatcherservlet을 만들기 떄문에 단위 테스트보다는 느리다
- 주의 사항
    - `MediaTypes.HAL_JSON`을 사용할때 **"s"**를 잊지말자!!
        - `import org.springframework.hateoas.MediaTypes;`를 사용해 Import 해야 함!!
    - Junit 버전에 맞게 import 해줘야함 안그러면 에러 발생
        - `//import org.junit.Test; ❌ Junit4버전`
        - `import org.junit.jupiter.api.Test; // 👍 Junit5버전`
- 예시 코드
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
  
      // 👉 Spring Boot는 자동으로 Jackson이 의존성주입이 되어이 있음
      @Autowired
      private ObjectMapper objectMapper;  

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
  
      @Test
      public void addBodyConent() throws Exception {

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

## Bean 가져오기
- 이슈 사항
  - 테스트 시 해당 Controller가 생성자로 다른 Bean을 주입받고있다면 에러가 발생한다.
      - 에러 코드
          - `Parameter 0 of constructor in com.yoo.restAPI.events.EventController required a bean of type 'com.yoo.restAPI.events.EventRepository' that could not be found.`
- 해결방법 : `@MockBean`을 활용하면 된다. - 단 해당 방법을 사용해도 해당 요청에 따른 Controller의 응답값은 null이기에 처리다 따로 필요하다.
    - `null point`에러를 처리하기 위해서는 **스터빙이** 필요하다
        - `Mockito.when(레포지토리.save(?)).thenReturn(?);`와 같이 `Mockito`를 사용하면 스터빙이 가능하다

- 예시 코드
  ```java
  // Controller
  @RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE )
  public class EventController {
      // 😱 문제가 되는 주입 부분
      private final EventRepository eventRepository;
      @PostMapping
      public ResponseEntity createEvent(@RequestBody Event event){
          // 저장
          Event newEvent =  this.eventRepository.save(event);
          // code ...
      }
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
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



## 반복되는 테스트 코드

- `@ParameterizedTest`, `@MethodSource`, `Stream<Arguments>`를 사용해서 해결 가능
- 사용 방법
    - 필수
        - `@ParameterizedTest` : 파라미터를 통한 테스트가 가능 
    - 이후 선택
        - `@ValueSource`: 한 개의 인수 입력 시 사용
        - `@CsvSource`: 한 개의 인수와 해당 인수를 넣었을 때의 결과값 입력 시 사용
        - `@NullSource`, `@EmptySource`, `@NullOrEmptySource`: null 또는 공백값에 대한 테스트 시 사용
        - `@EnumSource`: Enum 값에 대한 테스트 시 사용
        - ℹ️ `@MethodSource`: 테스트에 복잡한 인수를 제공하고자 할 때 메소드를 만들어서 사용
          - Type Safety를 위해서 `@MethodSource`위주로 사용하자
- 사용 예시
  ```java
  @SpringBootTest
  @AutoConfigureMockMvc
  public class wrapClass {
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
  }
  ```


## 테스트용 DB 설정
```properties
# ℹ️ 설정 방향
# 실사용 DB : `postgres`, 테스트 DB : `h2` 설정
```
- 방법
  - h2 DB를 test scope로 지정
       ```groovy
      dependencies {
        // ✨ Test scope로 변경
        testImplementation 'com.h2database:h2'
      }
      ```
  - properties 생성
    - profile 지정이 가능하게 끔 `"-"`를 사용해 구분
      - `application-test.properties`
        ```properties
        ## ℹ️ application-test.porperties - 파일 생성
        spring.datasource.username=sa
        spring.datasource.password=
        spring.datasource.url=jdbc:h2:mem:testdb
        spring.datasource.driver-class-name=org.h2.Driver
  
        spring.datasource.hikari.jdbc-url=jdbc:h2:mem:testdb
  
        spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
        ```  
  - 테스트 코드 내 사용할 properties 지정
    - `@ActiveProfiles("프로퍼티명")`
        ```java
        @SpringBootTest
        @AutoConfigureMockMvc
        @AutoConfigureRestDocs
        @Import(RestDocsConfiguration.class)
        @ActiveProfiles("test") // 👌 application-test.porperties 지정
        public class EventControllerTests { }
         ```

    
