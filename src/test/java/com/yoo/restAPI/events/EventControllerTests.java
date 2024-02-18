package com.yoo.restAPI.events;

//import org.junit.Test; ❌ Junit4버전

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoo.restAPI.common.RestDocsConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
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
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private ModelMapper modelMapper;

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
                .andDo(document("resources-query-events"))
        ;
    }

    @Test
    @DisplayName("기존의 이벤트를 하나 조회하기")
    void getEvent() throws Exception{
        // Given - 데이터 생성 (Insert)
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
                .andDo(document("resources-get-event"))
                ;
    }

    @Test
    @DisplayName("없는 이벤트 단건 조회 시 404 반환")
    void getEven404() throws Exception{
        // Given
        Integer id = 9999;

        // When
        this.mockMvc.perform(get("/api/events/"+id))
                // Then
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("이벤트를 정상적으로 수정하기")
    void updateEvent() throws Exception{
        // Given
        Event event =this.generateEvent(999);
        EventDTO eventDTO = this.modelMapper.map(event, EventDTO.class);
        String eventName = "Update Event";
        eventDTO.setName(eventName);

        /** Put, Patch 차이
         * - Put : 자원의 전체 교체 / 해당 리소스가 없으면 새롭게 생성
         * - Patch :    자원의 부분 교체
         *  */
        // When
        this.mockMvc.perform(patch("/api/events/{id}",event.getId())
                        // ☠️ 삽질함..  contentType, content 둘 제대로 확인안해서 body값이 계속 null.. 정신차리자
                        // -  content(MediaType.APPLICATION_JSON_VALUE)로 보냄..
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(this.objectMapper.writeValueAsString(eventDTO)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                // 👉 Link를 가지는지 체크
                .andExpect(jsonPath("_links.self").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("입력값이 비어있는 경우 이벤트 수정 실패")
    void updateEvent400_Empty() throws Exception{
        // Given
        Event event =this.generateEvent(999);
        EventDTO eventDTO = null;

        // When
        this.mockMvc.perform(patch("/api/events/{id}",event.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(this.objectMapper.writeValueAsBytes(eventDTO)))
                // Then
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("입력값이 잘못된 경우 이벤트 수정 실패")
    void updateEvent400_Wrong() throws Exception{
        // Given
        Event event =this.generateEvent(999);
        EventDTO eventDTO = this.modelMapper.map(event, EventDTO.class);
        // ❌ BasePrice가 Max보다 훨신 높음
        eventDTO.setBasePrice(9_999_999);
        eventDTO.setMaxPrice(10);

        // When
        this.mockMvc.perform(patch("/api/events/{id}",event.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(this.objectMapper.writeValueAsBytes(eventDTO)))
                // Then
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 업데이트 요청 실패")
    void updateEvent404() throws Exception{
        // Given
        EventDTO eventDTO = EventDTO.builder()
                .description("수정했습니다!! 그것도 방금!!")
                .build();

        this.mockMvc.perform(patch("/api/events/9999")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(this.objectMapper.writeValueAsString(eventDTO)))
                // Then
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    private Event generateEvent(int index) {
        Event event = Event.builder()
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
                .free(false)
                .offline(false)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return eventRepository.save(event);
    }


}
