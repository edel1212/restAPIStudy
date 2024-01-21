package com.yoo.restAPI.events;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
@RequiredArgsConstructor
// ⭐ RequestMapping을 사용해서 produces를 지정하면 하위 모든 Method의 반환 타입을 지정 가능하다!
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE )
public class EventController {

    private final EventRepository eventRepository;

    // Application에서 Bean등록 완료
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody EventDTO eventDTO){

        // 👉 modelMapper를 통해 DTO -> Entity 시킴
        Event event = modelMapper.map(eventDTO, Event.class);

        // 저장
        Event newEvent =  this.eventRepository.save(event);
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        event.setId(999);
        return ResponseEntity.created(createdUri).body(event);
    }

}
