package com.yoo.restAPI.events;

import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;




// ⭐ WebMvcLinkBuilder를 import 해줘야한다 .. 삽질함..
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
//import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo; ❌ 이거 아님 ...

@Controller
// ⭐ RequestMapping을 사용해서 produces를 지정하면 하위 모든 Method의 반환 타입을 지정 가능하다!
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE )
public class EventController {

    @PostMapping
    public ResponseEntity createEvent(@RequestBody Event event){
        /** Method에 path정보가 있을 경우 "methodOn()" 와 "해당함수명()" 를 사용해 추출해야 했음  */
        // URI createdUri = linkTo(methodOn(EventController.class).createEvent(event)).slash("{id}").toUri();

        URI createdUri = linkTo(EventController.class).slash("{id}").toUri();
        event.setId(999);
        return ResponseEntity.created(createdUri).body(event);
    }

}
