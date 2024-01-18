package com.yoo.restAPI.events;

//import org.junit.Test; ❌ Junit4버전
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test; // 👍 Junit5버전
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@RunWith(SpringRunner.class)    //Spring 테스트 컨텍스트를 관리하면서 테스트를 실행하는 데 사용되는 JUnit 러너입니다.
@WebMvcTest                     // MockMvc 빈을 자동으로 설정해준다 ___ 웹 관련 빈만 등록해 준다(슬라이스)
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

    // ⭐ @MockBean을 통해 가짜 객체 생성
    @MockBean
    private EventRepository eventRepository;

    @Test
    public void createEvent() throws Exception {
        /** Given */
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

        /**
         * 👉 스터빙 코드
         *    - 사용하지 않을 시 저장해도 null을 반환하기에 저장 시 진행 될 코드를 만드는것
         *    - Id를 지정해주는 것은 시퀀스로 자동 생성으로 할 것이기에 body 값에 없기 떄문임!
         * */
        event.setId(999);
        Mockito.when(eventRepository.save(event)).thenReturn(event);

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
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE)); // Content-Type 체크
    }
}
