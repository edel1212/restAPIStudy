package com.yoo.restAPI.events;

//import org.junit.Test; ❌ Junit4버전

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.restAPI.common.RestDocsConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs // 1 해당 어노테이션을 선언해서 사용한다 지정
@Import(RestDocsConfiguration.class)    // 💬 Docs 형식 pretty
@ActiveProfiles("test")
public class EventControllerTests {

    /**
     * - Spring Mvc 테스트에 있어서 가장 핵심 적인 클래스 이다.
     * - 웹서버를 띄우지 않기 떄문에 빠르다
     * - 디스패처서블릿을 만들기 떄문에 단위 테스트보다는 느리다
     * */
    @Autowired
    private MockMvc mockMvc;


    // 👉 Spring Boot는 자동으로 Jackson이 의존성주입이 되어이 있음
    @Autowired
    private ObjectMapper objectMapper;

    //@Test
    @DisplayName("정상 저장")
    public void createEvent() throws Exception {
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
                        .accept(MediaTypes.HAL_JSON) // HATOAS를 Import 해줘야 함
                        // 💬 Body 값 등록
                        .content(objectMapper.writeValueAsString(event))
                )
                .andDo(print())
                /** Then */
                .andExpect(status().isCreated())                     // 성공일 경우 201 반환
                .andExpect(jsonPath("id").exists())        // 응답 값에 id가 있는지 확인
                .andExpect(header().exists(HttpHeaders.LOCATION))    // 응답 로케이션 유무 확인
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE)) // Content-Type 체크
                .andExpect(jsonPath("id").value(Matchers.not(100)))                         // DTO에서 커트!! 그렇기에 없음
                .andExpect(jsonPath("free").value(Matchers.not(true)))                      // DTO에서 커트!! 그렇기에 없음
                .andExpect(jsonPath("eventStatus").value(Matchers.not(EventStatus.DRAFT.name()))) // DTO에서 커트!! 그렇기에 없음
                ;
    }

    //@Test
    @DisplayName("DTO로 받는데 추가적인값을 더 받을 경우 예외 발생")
    public void createEvent_Bad_Request() throws Exception {
        /** Given */
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("Rest API Test")
                .beginEventDateTime(LocalDateTime.now().minusDays(2))
                .closeEnrollmentDateTime(LocalDateTime.now())
                .endEventDateTime(LocalDateTime.now())
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .free(true)
                .location("공릉역")
                .eventStatus(EventStatus.DRAFT)
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
                .andExpect(status().isBadRequest())
        ;
    }

    //@Test
    @DisplayName("입력값 Null 검사")
    void createEvent_Bad_Request_Empty_Input()  throws  Exception{
        EventDTO eventDTO = EventDTO.builder().build();
        mockMvc.perform(
                        post("/api/events")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(eventDTO))
                )
                .andExpect(status().isBadRequest());
    }

    //@Test
    @DisplayName("입력값 유효성 검사")
    void createEvent_Bad_Request_Wrong_Input()  throws  Exception{
        EventDTO eventDTO = EventDTO.builder()
                .name("Spring")
                .description("Rest API Test")
                .beginEnrollmentDateTime(LocalDateTime.now())
                .closeEnrollmentDateTime(LocalDateTime.now())
                .beginEventDateTime(LocalDateTime.now().minusYears(100)) // ❌ 이벤트 시작 날짜가 종료 날짜보다 100년 빠름
                .endEventDateTime(LocalDateTime.now())
                .basePrice(10_000)
                .maxPrice(200)                                           // ❌ Base보다 Max가 훨씬 적음
                .limitOfEnrollment(100)
                .location("공릉역")
                .build();


        mockMvc.perform(
                        post("/api/events")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(eventDTO))
                )
                .andExpect(status().isBadRequest());
    }


    //@Test
    @DisplayName("ErrorSerializer 생성을 통해 JSON구조로 에러 받기")
    void createEvent_Bad_Request_Error_Serializer()  throws  Exception{
        EventDTO eventDTO = EventDTO.builder()
                .name("Spring")
                .description("Rest API Test")
                .beginEnrollmentDateTime(LocalDateTime.now())
                .closeEnrollmentDateTime(LocalDateTime.now())
                .beginEventDateTime(LocalDateTime.now().minusYears(100)) // ❌ 이벤트 시작 날짜가 종료 날짜보다 100년 빠름
                .endEventDateTime(LocalDateTime.now())
                .basePrice(10_000)
                .maxPrice(200)                                           // ❌ Base보다 Max가 훨씬 적음
                .limitOfEnrollment(100)
                .location("공릉역")
                .build();


        mockMvc.perform(
                        post("/api/events")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(eventDTO))
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                ;
    }

    //@Test
    @DisplayName("입력 값이 잘못된 경우 에러 발생 체크")
    void createEvent_Bad_Request_Wrong_input() throws Exception{
        EventDTO eventDTO = EventDTO.builder()
                .name("Spring")
                .description("Rest API Test")
                .beginEnrollmentDateTime(LocalDateTime.now())
                .closeEnrollmentDateTime(LocalDateTime.now())
                .beginEventDateTime(LocalDateTime.now().minusYears(100)) // ❌ 이벤트 시작 날짜가 종료 날짜보다 100년 빠름
                .endEventDateTime(LocalDateTime.now())
                .basePrice(10_000)
                .maxPrice(200)                                           // ❌ Base보다 Max가 훨씬 적음
                .limitOfEnrollment(100)
                .location("공릉역")
                .build();


        mockMvc.perform(
                        post("/api/events")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(eventDTO))
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
        ;
    }

    //@Test
    @DisplayName("입력 값이 잘못된 경우 에러 발생 체크")
    void createEvent_Success() throws Exception{
        EventDTO eventDTO = EventDTO.builder()
                .name("Spring")
                .description("Rest API Test")
                .beginEnrollmentDateTime(LocalDateTime.now())
                .closeEnrollmentDateTime(LocalDateTime.now().plusDays(7))
                .beginEventDateTime(LocalDateTime.now())
                .endEventDateTime(LocalDateTime.now().plusDays(10))
                .basePrice(10_000)
                .maxPrice(20_000)
                .limitOfEnrollment(100)
                .location("공릉역")
                .build();


        mockMvc.perform(
                        post("/api/events")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaTypes.HAL_JSON)
                                .content(objectMapper.writeValueAsString(eventDTO))
                )
                .andExpect(status().isCreated())
                .andDo(print())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
        ;
    }

    @Test
    @DisplayName("HAL_JSON 체크 ")
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
                .andExpect(jsonPath("_links.update-event").exists());
    }

    //@Test
    @DisplayName("create-event :: snippets 생성")
    public void createEvent_Snippets() throws Exception {
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


    @Test
    @DisplayName("create-event :: profile 추가")
    public void createEvent_Snippets_Add_Profile() throws Exception {
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
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self")
                                , linkWithRel("query-events").description("link to query-events")
                                , linkWithRel("update-event").description("link to update-event")
                                // ✏️ 프로필 검사
                                , linkWithRel("profile").description("profile!!!!")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                , headerWithName(HttpHeaders.ACCEPT).description("accept")
                        ),
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
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location Header")
                                , headerWithName(HttpHeaders.CONTENT_TYPE).description("Content Type")
                        ) ,
                        responseFields(
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
                                , PayloadDocumentation.fieldWithPath("_links.profile.href").description("프로필 이랍니다")
                        )

                ))

        ;
    }
}
