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

import javax.swing.text.Document;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
   private EventRepository eventRepository;

    @Test
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
                // 💬 예외 발생 시 페이지가 전이될 index 링크 추가
                .andExpect(jsonPath("_links.index").exists())
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


    @Test
    @DisplayName("30개의 이벤트를 10씩  두번쨰 페이지 조회")
    public void queryEvents() throws Exception{
        // Given
        IntStream.rangeClosed(0,30).forEach(this::generateEvent);

        // When
        this.mockMvc.perform(get("/api/events")
                        .param("page","1")
                        .param("size","10")
                        .param("sort","name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[*]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"))
        ;
    }

    @Test
    @DisplayName("기존의 이벤트를 하나 조회하기")
    void getEvent() throws Exception{
        // Given
        Event event = this.generateEvent(100);

        // When
        this.mockMvc.perform(get("/api/events/{id}",event.getId()))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(print())
                ;
    }

    private Event generateEvent(int index) {
        Event event = Event.builder()
                .name("event"+ index)
                .name("description"+ index)
                .build();
        return eventRepository.save(event);
    }


}
