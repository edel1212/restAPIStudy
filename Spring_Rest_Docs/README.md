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
- ### Snippet 생성 (응답/ 요청 -JSON 경우)
  -  👉 주의사항
    -   `Reuqest/Response` 모든 필드 값이 `Null or 필수 값X`이라 해도 생성하려는 Snippet에 정의 되어 있어야 한다
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
- ### Snippet 생성 (응답 - PathValriable 경우)
  - ℹ️ `RestDocumentationRequestBuilders`를 사용해서 `get()`요청을 진행해야한다.
  - uri 내 `{PathValue}`형식으로 값을 지정해줘야한다
  ```java
  class TestClss{
    @DisplayName("@PathVariable 경우")
    @Test
    public void getPathVariable() throws Exception{
      mockMvc.perform(RestDocumentationRequestBuilders.get("/poi-icon/{iconSetId}", poiIconSetRes.getIconSetId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andDo(print())
      .andDo(document("스니펫명",
         // PathVariable 생성
                    pathParameters(
                            parameterWithName("iconSetId").description("아이콘셋 ID")
                    )
      )
  }
  ```
- ### Snippet 생성 (응답 - Multupart 경우)
  - ℹ️ `RestDocumentationRequestBuilders`를 사용해서 `get()`요청을 진행해야한다.
  - uri 내 `{PathValue}`형식으로 값을 지정해줘야한다
  ```java
  class TestClss{
    @DisplayName("@PathVariable 경우")
    @Test
    public void getPathVariable() throws Exception{
     // Given
        POIIconSetReq poiIconSetReq = POIIconSetReq.builder().build();
        MockMultipartFile poiIconSetReqPart = super.makeJSONPart(poiIconSetReq, "poiIconSetReq");

        MockMultipartFile defaultIcon = new MockMultipartFile("defaultIcon", "defaultIcon.png", MediaType.IMAGE_PNG_VALUE, "dummy data".getBytes());
        MockMultipartFile step1Icon = new MockMultipartFile("step1Icon", "step1Icon.png", MediaType.IMAGE_PNG_VALUE, "dummy data".getBytes());
        MockMultipartFile step2Icon = new MockMultipartFile("step2Icon", "step2Icon.png", MediaType.IMAGE_PNG_VALUE, "dummy data".getBytes());
        MockMultipartFile step3Icon = new MockMultipartFile("step3Icon", "step3Icon.png", MediaType.IMAGE_PNG_VALUE, "dummy data".getBytes());
        MockMultipartFile threeDimensions = new MockMultipartFile("threeDimensions", "threeDimensions.glb", String.valueOf(MediaType.valueOf("model/gltf-binary")), "dummy data".getBytes());

        // When & Then
        this.mockMvc.perform(multipart("/poi-icon")
                        .file(poiIconSetReqPart)
                        .file(defaultIcon)
                        .file(step1Icon)
                        .file(step2Icon)
                        .file(step3Icon)
                        .file(threeDimensions)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                .andDo(print())
                .andDo(document("register-poi-icon-set",
                        // ℹ️ 파일 데이터 Part
                        requestParts(
                                partWithName("poiIconSetReq").description("아이콘 셋의 정보 JSON 데이터"),
                                partWithName("defaultIcon").description("기본 아이콘 이미지 파일").optional(),
                                partWithName("step1Icon").description("1단계 아이콘 이미지 파일").optional(),
                                partWithName("step2Icon").description("2단계 아이콘 이미지 파일").optional(),
                                partWithName("step3Icon").description("3단계 아이콘 이미지 파일").optional(),
                                partWithName("threeDimensions").description("3D 모델 파일 (threeDimensions)").optional()
                        ),
                        // ℹ️ JSON 데이터 Part
                        requestPartFields("poiIconSetReq",
                                fieldWithPath("iconSetName").description("아이콘 셋의 이름"),
                                fieldWithPath("description").description("아이콘 셋의 설명").optional(),
                                fieldWithPath("step1Color").description("1단계 색상").optional(),
                                fieldWithPath("step2Color").description("2단계 색상").optional(),
                                fieldWithPath("step3Color").description("3단계 색상").optional(),
                                fieldWithPath("iconSetDisplayType").description("아이콘 셋의 표시 유형 [TWO_DIMENSIONS, THREE_DIMENSIONS, ALL]")
                        ),
                ))
        ;
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
  - 요창하는 데이터 형식에 따라 다르므로 공식 문서를 확인하자
    - [링크](https://github.com/spring-projects/spring-restdocs/tree/main/spring-restdocs-core/src/main/resources/org/springframework/restdocs/templates/asciidoctor)
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
    - `request-part-fields` 커스텀
      ```properties
      // ℹ️ request-part-fields.snippet
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
    - `request-parts` 커스텀
      ```properties
      // ℹ️ request-parts.snippet
      |===
      |필드명|필수 값 여부|설명

      {{#requestParts }}
      |{{#tableCellContent}}`+{{name}}+`{{/tableCellContent}}
      |{{#tableCellContent}}{{#optional}}false{{/optional}}{{^optional}}true{{/optional}}{{/tableCellContent}}
      |{{#tableCellContent}}{{description}}{{/tableCellContent}}
      {{/requestParts}}
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
