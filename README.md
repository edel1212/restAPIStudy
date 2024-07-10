# RestAPIStudy

## 목차

- [SpringBoot 이용간 유용한 정보](https://github.com/edel1212/restAPIStudy/tree/main/Spring_Tips)

  
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

### 원하는 Snippets 커스텀 방법
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



