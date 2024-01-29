package com.yoo.restAPI.events;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
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

    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors){
        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(errors);
        }

        eventValidator.validate(eventDTO, errors);

        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(errors);
        }

        // 👉 modelMapper를 통해 DTO -> Entity 시킴
        Event event = modelMapper.map(eventDTO, Event.class);

        // 👉 원래는 서비스에서 로직 구현이 필요하나 간단한 로직이니 스킵함
        event.update();

        // 저장
        Event newEvent =  this.eventRepository.save(event);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());

        URI createdUri = selfLinkBuilder.toUri();

        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withSelfRel());   // 👉 withSelfRel()를 사용해서 자기 자신 사용
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        return ResponseEntity.created(createdUri).body(eventResource);
    }

}
