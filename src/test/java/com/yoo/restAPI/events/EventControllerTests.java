package com.yoo.restAPI.events;

//import org.junit.Test; ❌ Junit4버전
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; // 👍 Junit5버전
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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

    @Test
    @DisplayName("정상 저장")
    public void createEvent() throws Exception {
        /** Given */
        EventDTO event = EventDTO.builder()
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

    @Test
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

    @Test
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

    @Test
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

}
