package com.yoo.restAPI.events;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Test
    public void createEvent() throws Exception {
        mockMvc.perform(
                        post("/api/events/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaTypes.HAL_JSON) // HATOAS를 Import 해줘야 함
                )
                .andExpect(status().isCreated());
    }
}
