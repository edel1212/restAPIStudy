# Rest Docs

- 테스트 코드 기반으로 Restful API 문서를 돕는 라이브라리이다.
- API 문서화 내용과 테스트 코드가 일치하지 않으면 `build file`생성 실패한다
  - 검증된 테스트 코드로만 문서를 만들기에 안정성을 보장한다.
- Swagger와 차이점
  - Swagger는 테스트하는 용도에 조금 더 특화 되어 있다
    - 테스트 기반이 아니기에 문서가 100% 정확하다고 확신할 수 없다.
      - 애노테이션을 통해 명세를 작성하기에 명세를 위한 코드들이 많이 붙게되어 전체적으로 가독성이 떨어진다.
        - 예시 : `@ApiModelProperty(value = "카카오 image url", required = true, example = "\"http://k.kakaocdn.net\"")`

- 사용 흐름
  - 1 . `TestCode` 기반으로 작성
  - 2 . 테스트가 성공하면 해당 테스트의 `Snippet`이 만들어진다.
    - 문서의 생성 위치는 따로 설정하지 않았다면 `build -> generated-snippets -> 설정한 doucument명` 위치에 생성된다.
  - 3 .  `Snippet` 파일들을 `index.adoc`파일에 연결
  - 4 . 프로젝트 `build` 시 `index.adoc` -> `index.html`로 변환
  


## Snippet
- ### build.gradle 설정
  ```groovy
  plugins {
      
      id 'org.asciidoctor.jvm.convert' version '3.3.2'
  }
  
  configurations {
      compileOnly {
          extendsFrom annotationProcessor
      }
      // ✨ asciidoc 생성을 위해 추가
      asciidoctorExtensions
  }
  
  ext {
      set('snippetsDir', file("build/generated-snippets"))
  }
  
  dependencies {
      // asciidoc 생성
      asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor'
  }
  
  tasks.named('test') {
      outputs.dir snippetsDir
      useJUnitPlatform()
  }
  
  tasks.named('asciidoctor') {
      inputs.dir snippetsDir
      configurations 'asciidoctorExtensions'
      dependsOn test
  }
  
  bootJar {
      dependsOn asciidoctor
  
      // build 경로 안에 있는 index.html을 밖으로 꺼내준다.
      doLast {
          copy {
              from tasks.asciidoctor.outputs.files.singleFile
              into "src/main/resources/static/docs"
          }
      }
  }
  ```
- ### Snippet 생성
  ```java
  @Log4j2
  @AutoConfigureMockMvc
  @AutoConfigureRestDocs
  class MemberControllerTest extends BaseTestLogic {
      @Autowired
      public MockMvc mockMvc;
  
  @DisplayName("회원 가입 요청")
  @Test
  public void signUpMember_success() throws  Exception{
      // Given
      /** Code.. */
    
      // When & Then
      this.mockMvc.perform(post("/member/sign-up")
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaTypes.HAL_JSON_VALUE)
                      .content(this.objectMapper.writeValueAsString(signUpReq)))
              .andDo(print())
              /** Test Logic */
              .andDo(document("sign-up",  // 👉 adocs명 지정
                      // 👉 Link 정보 생성
                      links(
                              linkWithRel("self").description("link to self - 회원 가입")
                              , linkWithRel("sign-in").description("로그인")
                              , linkWithRel("profile").description("Docs")
                      ),
                      // 👉 요청 Header 정보 생성
                      requestHeaders(
                              headerWithName(HttpHeaders.CONTENT_TYPE).description(MediaType.APPLICATION_JSON_VALUE)
                      ),
                      // 👉 응답 Header 정보 생성
                      responseHeaders(
                              headerWithName(HttpHeaders.CONTENT_TYPE).description(MediaType.APPLICATION_JSON_VALUE)
                      ),
                      // 👉 요청 파라미터 생성
                      requestFields(
                              fieldWithPath("id").description("회원가입에 사용한 ID"),
                              fieldWithPath("password").description("패스워드")
                      ),
                      // 👉 응답 값 생성
                      responseFields(
                              fieldWithPath("id").description("계정을 식별할 ID값 중복 불가능"),
                              fieldWithPath("_links.self.href").description("요청했던 링크"),
                              fieldWithPath("_links.profile.href").description("Docs 링크")
                      )
              ));
      }
  }    
  ```
- ### Snippet 커스텀 - Pretty
  - 필수는 아니고 선택이다.
  - Snippet 내용 자체가 정렬되지 않은 문자열 형태은 것을 커스텀을 통해 수정이 가능하다.
  - 사용 방법
    - `src/test/~~/comm` 디렉토리 생성 후 설정 파일 생성
    - `@TestConfiguration` 테스트용으로 사용될 설정임을 설정
    - Bean 주입
    - 설정 파일
      ```java
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
    - 테스트 코드 - 적용하는 곳
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


## Snippet 커스텀 - 결과 형식
- 문제
  - Snippet이 기본으로 제공하는 틀을 사용하면 `PayloadDocumentation.fieldWithPath("name").description("Name fof new Event")` 와 같이 필드명, 설명 만 나오게 된다.
- 해결 방법
  - `src/test/resources/org/springframework/restdocs/templates` 경로의 폴더를 만든다
  - 커스텀을 원하는 `스니팻명.snippet`으로 파일을 만든 후 원하는 형식으로 내용을 채워준다.
  - 예시
    - `request-fields` 커스텀
      ```properties
      // ℹ️ request-fields.snippet
      |===
      |필드명|타입|필수 값 여부|설명

      {{#fields}}
      |{{#tableCellContent}}`+{{path}}+`{{/tableCellContent}}
      |{{#tableCellContent}}`+{{type}}+`{{/tableCellContent}}
      |{{#tableCellContent}}{{#optional}}false{{/optional}}{{^optional}}true{{/optional}}{{/tableCellContent}}
      |{{#tableCellContent}}{{description}}{{/tableCellContent}}
      {{/fields}}
      |===
        ```
    - `query-parameters` 커스텀
      ```properties
      // ℹ️ query-parameters.snippet
      |===
      |Parameter|필수 값 여부|설명

      {{#parameters}}
      |{{#tableCellContent}}`+{{name}}+`{{/tableCellContent}}
      |{{#tableCellContent}}{{#optional}}false{{/optional}}{{^optional}}true{{/optional}}{{/tableCellContent}}
      |{{#tableCellContent}}{{description}}{{/tableCellContent}}

      {{/parameters}}
      |===
      ```    

## AscIIdocs

```properties
# ℹ️ 설정 방법은 SpringBoot 버전에 따라 다르니 맞춰서 적용하자
```
- 기본 틀 - index.adoc 파일 
  - [링크](https://github.com/edel1212/restAPIStudy/blob/main/src/docs/asciidoc/index.adoc)
- 이후 build를 실행하면 `내가 지정했던 위치`로 변환된 `index.html`이 생성된다.
- 주의 할점
  - Test 코드 중 실패하는 코드가 있을 경우 에러를 반환하므로 주의하자!
