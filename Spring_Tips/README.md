# Spring Tips

## Dependencies 스코프 관련

- `Test Scope`로 설정 시 프로젝트가 실행 시 해당 디팬던시는 사용되지 않는다.
    - 필요 경우에 따라 **스코프를 변경하여 개발하는 습관**을 들이자.
      - 테스트에 사용할 DB는 스코프를 그에 맞게 변경 
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

## Entity 생성

- Java Bean 스팩에 맞게 구현하며,  빌더 패턴만 사용하게 끔 하지 말자
- `@Builer`는 매개변수가 없는 **디폴트 생성자를 생성** 메서드를 **만들어 주지 않는다.**
    - 따라서 `@NoArgsConstructor`를 사용하라면 `@AllArgsConstructor`는 항상 같이 따라 다닌다 보면 된다.
- `@EqualsAndHashCode`를 사용하면 `StackOverFlow`가 생길 수 있는 일을 미연에 방지가 가능하다.
    - 지정한 값을 기준으로 `Entity`간의 비교가 가능해지기 떄문
    - Set형태로 여러개 지정이가능하다
- `@Data`를 사용하지 않는 이유 또한 위와 같은 이유이다 **EqualsAndHashCode**를 모든 필드로 만들어 버림
  - 다른 Entity를 `상호참조`로 인해 `StackOverFlow`가 발생 할 수 있다.

- ### Entity 코드
  ```java
  @Builder 
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
  // 😱 @Data  <<가 있지만 Entity에서는 사용하지말자 위에서 말한 EqualsAndHashCode를 모든 필드를 대상으로 만들기 떄문이다.
  public class Event {
    private Integer id;
    private boolean offline;
    /** code.. */
  }
  ```

## DTO
- DTO 사용 이유 ?
  - 받은 값을 제한할 수 있다.
    - 구현 시 Entity는 최대한 건들지 않는 것이 중요하다
        - 가장 중요 이유 : 무결성 유지하기 위해서이다
            - Entity를 수정하다보면 나중에 값이 어디서 변경된지 찾기 힘들어진다.

## ModelMapper

- `DTO -> Entity` 혹은 `Entity -> DTO`와 같은 변환에 유용한 **라이브러리**이다

- 사용 방법
  - 의존성 추가
      - `implementation group: 'org.modelmapper', name: 'modelmapper', version: '3.2.0'`
  - Bean 등록
    ```java
    @SpringBootApplication
    public class RestApiApplication {
      // Application Start Code 생략 ...
      @Bean
      public ModelMapper modelMapper(){ return new ModelMapper(); }
    }
    ```
  - 비즈니스 로직 사용
    -  DTO -> Entity 변환 
      - `Event event = modelMapper.map(eventDTO, Event.class);`

- ### ModelMapper - 사용 시 TestCode 문제
  - 이슈 사항
    - `@WebMvcTest` 테스트는 웹 관련 빈만 등록하기 떄문
      - 다른 빈이 필요하면 직접 추가해야 함
      - 특히, 테스트 시 ModelMapper와 같은 빈은 자동으로 주입되지 않는다
      - `@MockBean`을 통해 사용하여 필요한 객체를 주입이 가능하나 번거롭다.

  - 해결 방법 
      - 테스트 시 전체 프로세싱으로 테스트한다 (통합 테스트 환경) 
        - `@SpringBootTest,@AutoConfigureMockMvc`를 통해 스터빙을 끊고 자동으로 전체 주입 받는 형식으로 구현

  - 🤔 고민 했던 문제        
    - 위와 같이 통합 테스트 환경으로 테스트를 진행하는게 맞는지 고민함
        - 강의에서는 웹쪽 관련 테스트 코드는 해당 방법일 선호하며 추천한다고 함
            - 웹 테스트 시 많은 모킹이 필요여 번거롭고 관리 또한 힘들다
                - 코드가 바뀌면서 전체적인 설정이 바뀔 수도 있고 그에 따라 위험도가 올라가는 문제 또한 있다
            - 시간이 많이 들며 그럴 바에는 차라리 전체 빈을 주입받아 테스트하는 것이 효율적이라 하였다.
    - `@SpringBootTest`를 사용하면 Application에 설정되어있는 모든 빈을 주입하여 테스트하며 실제 어플리케이션과 가장 가까운 형태로 테스트가 가능하다.
        - API 테스트 시 슬라이싱 테스트 보다 해당 방법을 선호한다.


## 내부문자열을 외부설정으로 빼기
```properties
# ℹ️ 코드 내 작성되어 있던 문자열을 `properties`에 작성 후 Class에 매핑
#    ㄴ> 가독성이 좋아지며 한번에 관리하기도 좋아짐
```
- ### dependencies
  - `annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'` 

- ### application.properties
  ```properties
  my-app.username = "edel1212"
  my-app.password = "123"
  ```
- ### MappingClass
    - `@ConfigurationProperties(prefix = "my-app")` 설정을 하면 시작 명칭으로도 가능
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
- ### Test Code
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

- ### `@JsonUnwrapped`
  - 중첩된 객체의 필드를 상위 객체의 필드로 평탄화하여 JSON 형식으로 직렬화 및 역직렬화할 수 있습니다.
    - 직렬화를 할때 Java Bean스팩에 맞게 직렬화를 한다.
  - 예시
    ```java
    public class Employee {
      private String name;
      @JsonUnwrapped
      private Department dept;
    
    }
    ```


## 개발 시 참고 사항
- ### H2-Base `In-Memory DB` 공유
  - 이슈
    - 테스트 코드 작성 시 `H2-Base`를 사용한 `In-Memory DB`를 사용한다 해도 다건의 테스트의 경우 데이터가 공유 된다.
  - 해결 방법
    - 어쩔 수 없는 공유된 데이터의 경우 `@Before`어노테이션을 활용해서 지워주자
        - Ex) 인증 정보 추가 시 계정이 계속해서 같은 ID가 추가되는 문제가 발생
    - `@Before`을 사용하지 않더라도 중복 요청이 발생하지 않도록 로직 내에서 처리하는 방식으로도 가능하다.
    - 지정 DB 데이터를 지워버리기 `repository.deleteAll()` 사용

- ### `MockMvc`테스트에서 결과값을 받아서 Return 받기
    - `MvcResult`사용하면 `perform()`결과를 받아올 수 있다.
    - 예시 코드
        ```java
        @SpringBootTest
        @AutoConfigureMockMvc
        class AuthControllerTest {
            @Autowired
            MockMvc mockMvc;
        
            @Autowired
            MemberRepository memberRepository;
        
            @DisplayName("로그인_통과")
            @Test
            void login_통과() throws Exception {
                String requestMemberName = "seohae";
                // 1. MvcResult 생성 
                MvcResult result = mockMvc.perform(post("/auth/login")
                                .param("memberName", requestMemberName)
                                .param("password", "12341234"))
                        .andExpect(status().isOk())
                        .andReturn();
                
                // 2. 응답 body 값을 문자열로 변환
                String response = result.getResponse().getContentAsString();
                // 3. Jackson2JsonParser 생성
                var parser = new Jackson2JsonParser();
                // 4. JSON구조에서 지정 key 값을 가져옴
                String memberName = parser.parseMap(response).get("memberName").toString();
            }
        }
    ```

- ### SpringSecurity에서 인증정보를 받는 방법
  - 방법
    - 첫번째 : 코드 내 직접 `SecurityContextHolder`에 접근하여 받기
        - `SecurityContextHolder.getContext().getAuthentication();`을 통해 받아올 수 있다.
      ```java
      public class wrapClass{
          public void getPrincipal(){
              Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
              UserDetails userDetails = (UserDetails)principal;
              String username = principal.getUsername();
              String password = principal.getPassword();
          }     
      }
      ```
    - 두번째 : 어노테이션을 통해 받기
      - `@AuthenticationPrincipal` : User 객체 또는 상속 개체로 받음
      - `@AuthenticationPrincipal(experssion = "대상키")` : 대상 키를 바로 뽑아 쓸 수 있음
      - 예시 코드
        ```java
        public class wrapClass{
            @GetMapping("/")
            public void test(@AuthenticationPrincipal UserAccount userAccount){ 
                /** ℹ️ 주입 받는객체는 User 또는를 상속 구현한 객체여야한다. */ 
            }   
        }
        ``` 
        - `AnnotationClass` 사용으로 더욱 간소화 가능
            - 메타 어노테이션을 지원하므로 간소화 가능 
              - `@만든-어노테이션`로 불러오기가 가능해짐
            - 예시 코드
                ```java
                @Target(ElementType.PARAMETER)      // 파라미터 형태로 사용 명시
                @Retention(RetentionPolicy.RUNTIME) // 언제까지 해당 어노테이션 지정 여부 : 런타임
                /**
                * ✅ 접근 대상이 anonymousUser 권한이 들어올 경우 User 객체를 타고 넘어오지 않아 응답 값이
                *    anonymousUser라는 문자열로 들어오기에 해당 expression의 유연함을 활용해서 적용해주자
                * */
                @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : targetKey")
                public @interface CurrentUser { }
                ```
     
- ### Log 컬러 추가
  - 설정 방법 
    ```properties
    spring.output.ansi.enabled=always
    ```

- ### P6psy
```properties
# ℹ️ SpringBoot 3.x.x 버전 이상부터는 적용 Dependecies 버전이 올라감
```
  - dependencies
    - `implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'`
  - 설정 Class
    ```java
    public class CustomP6spySqlFormat extends JdbcEventListener implements MessageFormattingStrategy {
    
        @Override
        public void onAfterGetConnection(ConnectionInformation connectionInformation, SQLException e) {
            P6SpyOptions.getActiveInstance().setLogMessageFormat(getClass().getName());
        }
    
        @Override
        public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
            StringBuilder sb = new StringBuilder();
            sb.append(category).append(" ").append(elapsed).append("ms");
            if (StringUtils.hasText(sql)) {
                sb.append(highlight(format(sql)));
            }
            return sb.toString();
        }
    
        private String format(String sql) {
            if (isDDL(sql)) {
                return FormatStyle.DDL.getFormatter().format(sql);
            } else if (isBasic(sql)) {
                return FormatStyle.BASIC.getFormatter().format(sql);
            }
            return sql;
        }
    
        private String highlight(String sql) {
            return FormatStyle.HIGHLIGHT.getFormatter().format(sql);
        }
    
        private boolean isDDL(String sql) {
            return sql.startsWith("create") || sql.startsWith("alter") || sql.startsWith("comment");
        }
    
        private boolean isBasic(String sql) {
            return sql.startsWith("select") || sql.startsWith("insert") || sql.startsWith("update") || sql.startsWith("delete");
        }
    
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    @Configuration
    public class P6spyLogMessageFormatConfig {
        @PostConstruct
        public void setLogMessageFormat() {
            P6SpyOptions.getActiveInstance().setLogMessageFormat(CustomP6spySqlFormat.class.getName());
        }
    }
    ```

- ### 테스트 코드 응답 값 한글 처리
  - 이슈 사항
    - 테스트 코드 작성 후 응답 결과가 한글이 깨지는 이슈 발생
  - 해결 방법
    - properties 설정으로 해결
      ```properties
      server:
      # 한글 반환 처리
      servlet:
        encoding:
          force-response: true
      ```
- ### 엄격한 Request 값 제한
```properties
# ℹ️ 사용 유무는 권장이 아니며 개발 상황에 맞게 사용한다.
#  - 좀 더 엄격하게 사용자의 요청값에 제한을 두는 것이다.
```
- 흐름
  - 사용자 : `{name : "yoo",age : 100}` 로 서버로 요청 보냄
  - 서버 : Class field 값 `private String name`만 존제 
  - 서버가 허용하지 않는 `age`를 넘기므로 예외를 발생 시킴
- 설정 방법
  - properties 설정
    - `spring.jackson.deserialization.fail-on-unknown-properties=true` 

- ### Request 값 체크
  - dependencies 
    - `implementation 'org.springframework.boot:spring-boot-starter-validation'`
  - DTO Class
    -  `@NotEmpty`, `@NotNull`, `@Min(0)`, `@Max(0)` 등등 사용하여 검증
        ```java
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        public class LoginReq {
            @NotNull
            private String id;
            @NotNull
            private String password;
        }
        ```
  - Controller
    - Parameter 내 `@Valid` 지정 감시 대상으로 설정
    - DTO내 검증 기준에 맞지 않으면 `BindingResult` 객채 내 에러를 담고 있음
        ```java
        public class MemberController{
            @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity<EntityModel<JwtToken>> signIn(@Valid @RequestBody LoginReq loginReq
                    // ℹ️ 해당 객체에 검증 결과를 담고 있음
                    , BindingResult bindingResult){
                // 값 검증
                if(bindingResult.hasErrors()) throw new InputValidException();
                // code..
                return ResponseEntity.ok().body(entityModel);
            }
        }
        ```

- ### Request 값 체크 - 더 정밀한 검증
  - 검증 처리 Class 생성 
    ```java
    @Component // Bean 등록
    public class EventValidator {
        /**
         * ℹ️ 실제 검증을 처리할 Method
         * - 각각 Parmamter로 ( 검증 대상DTO, 예외를 핸들링할 객체 )
         * */
        public void validate(EventDTO eventDTO, BindingResult bindingResult){
            // 👉 최대 값을 넘는지 체크하는 로직
            if(eventDTO.getBasePrice() > eventDTO.getMaxPrice()
                && eventDTO.getMaxPrice() > 0 ){
                // 👉 rejectValue()를 통해 에러 주입 ( 필드명, 에러코드, 에러 메세지 )
                bindingResult.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
                bindingResult.rejectValue("maxPrice", "wrongValue", "MaxPrice is wrong");
            }//if
    
            // 예외 처리 두번째
            LocalDateTime eventEndTime =  eventDTO.getEndEventDateTime();
            if(eventEndTime.isBefore(eventDTO.getBeginEventDateTime())){
                bindingResult.rejectValue("endEventDateTime", "wrongValue", " endEventDateTime is wrong");
            }
    
            /** 위와 같은 방식으로 차례차례 검증 로직을 늘려 전부 Pass해야 정상 Request로 지정 */
        }
    }
    ```
  - Controller
    ```java
    public class EventController{
        // 👉 의존성 주입
        private final EventValidator eventValidator;
        
        @PostMapping(value = "/event", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<EntityModel<JwtToken>> eventTest(@Valid @RequestBody EventDTO eventDTO
                , BindingResult bindingResult){
            
            // 👉 검증 로직으로 확인
            eventValidator.validate(LoginReq, bindingResult);
            
            // 값 검증
            if(bindingResult.hasErrors()) throw new InputValidException();
            // code..
            return ResponseEntity.ok().body(entityModel);
        }
    }
    ```

- ### 복합키 처리 방법
  - 복합키 처리 방법에는 `@EmbeddedId`, `@IdClass`가 있으며, 진행하는 프로젝트 스타일에 맞춰 사용하는 것이 중요하다.
  - 차이
    - `@EmbeddedId` : 좀 더 객체 지향에 가깝다
      - Query : `select p.id.id1, p.id.i2 from Parent p`
    - `@IdClass` : 좀 더 RDBMS에 가깝다
        - Query : `select p.i1, p.id2 from Parent p`
  - 사용법
    - #### `@EmbeddedId`
      - PK Class
        - Class에 `@Embeddable` 지정
        - 기본 생성자 필수
        - `Serializable` 인터페이스 구현
        - equals, hashCode 구현(`@EqualsAndHashCode`) - 식별자로 사용하기 위함
      - Entity Class
        - 해당 PK 필드 `@EmbeddedId` 적용
        - 해당 PK Class 자체에서 equals, hashCode 사용중 이므로 `@EqualsAndHashCode` 불필요
      - 예시 
        ```java
        @Embeddable
        @NoArgsConstructor(access = AccessLevel.PROTECTED)
        @EqualsAndHashCode
        @AllArgsConstructor
        @Getter
        @Builder
        public class MemberPK implements Serializable {
            private String name;
            private Integer SSID;
        }
        
        ///////////////////////////////////////////////////////
        
        @Entity
        @Getter
        @AllArgsConstructor
        @NoArgsConstructor(access = AccessLevel.PROTECTED)
        @Builder
        public class Member {
        
            @EmbeddedId
            private MemberPK id;
        
            // code ...
        }
        ```
    - #### `@IdClass`
      - PK Class
        - 기본 생성자 필수
        - `Serializable` 인터페이스 구현
        - equals, hashCode 구현(`@EqualsAndHashCode`) - 식별자로 사용하기 위함
      - Entity Class
        - `@IdClass(만들어진 PK클래스.class)` 지정 필요
        - 각각의 필드를 재정의 하면된다.
          - `@Id`으로 PK 다중 지정이 가능해짐 
      - 예시
        ```java
        
        @Entity
        @EqualsAndHashCode(onlyExplicitlyIncluded = true)
        @NoArgsConstructor(access = AccessLevel.PROTECTED)
        @AllArgsConstructor
        @Builder
        public class GrandParent {
            @Id
            @EqualsAndHashCode.Include
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long grandParentId;
        
            private String grandParentName;
        }
        
        ///////////////////////////////////////////////////////
        
        @EqualsAndHashCode(onlyExplicitlyIncluded = true)
        @NoArgsConstructor(access = AccessLevel.PROTECTED)
        @AllArgsConstructor
        @Builder
        public class ParentPK implements Serializable {
        
            @EqualsAndHashCode.Include
            private Long parentId;
        
            @EqualsAndHashCode.Include
            private Long grandParent;
        
        }
        
        ///////////////////////////////////////////////////////
        
        @IdClass(ParentPK.class)
        @Entity
        public class Parent {
            
            @Id
            @Column(name= "parent_id")
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long parentId;
        
            @Id
            @ManyToOne
            @JoinColumn(name = "grand_parent_id")
            private GrandParent grandParent;
        
        }
        ```

- ### Entity 생성자
  - JPA는 리플렉션을 사용하여 엔티티 인스턴스를 생성할 때 매개변수가 없는 기본 생성자가 필요하다.
    - 개발자가 명시적으로 기본 생성자를 제공하지 않으면, 컴파일러는 자동으로 생성 한다
      - 개발자가 명시적으로 기본 생성자를 제공하지 않으면, 컴파일러는 자동으로 생성합니다.
  - Builder Pattern을 사용할 경우에도 생성자 메서드 명시가 필요하다. 
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)`로 접근제어를 하는 이유?
    - Entity 연관 관계에서 지연 로딩의 경우에는 실제 Entity가 아닌 ⭐️`proxy`객체를 통해서 조회를 한다.
      - 프록시 객체를 사용하기 위해서 JPA 구현체는, 실제 엔티티의 기본 생성자를 통해 프록시 객체를 생성하는데, 이 때 접근 권한이 `private`이면 **`proxy` 객체를 생성할 수 없기 떄문이다.**
        - 👉 단! 즉시로딩으로 구현하게 되면, 접근 권한과 상관없이 `proxy`객체가 아닌 실제 `Entity`를 생성하므로 문제가 생기지 않는다 
  - 사용 예시
    ```java
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // 생성자를 통해서 값 변경 목적으로 접근하는 메시지들 차단
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Entity
    public class User {
        @Id
        @EqualsAndHashCode.include
        private String id;
        private String password;
    }
    ```
       



