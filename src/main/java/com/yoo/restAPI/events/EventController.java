package com.yoo.restAPI.events;

import com.yoo.restAPI.index.IndexController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
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
            EntityModel<ErrorDTO> errorModel = this.makeErrorDTO(errors);
            return ResponseEntity.badRequest().body(errorModel);
        }// if

        eventValidator.validate(eventDTO, errors);

        if(errors.hasErrors()){
            EntityModel<ErrorDTO> errorModel = this.makeErrorDTO(errors);
            return ResponseEntity.badRequest().body(errorModel);
        }// if

        // 👉 modelMapper를 통해 DTO -> Entity 시킴
        Event event = modelMapper.map(eventDTO, Event.class);

        // 👉 원래는 서비스에서 로직 구현이 필요하나 간단한 로직이니 스킵함
        event.update();

        // 저장
        Event newEvent =  this.eventRepository.save(event);

        // 💬 EntityModel<대상>을 통해 _Link를 생성 할 수 있는 HATEOAS 컨테이너 객체 생성
        EntityModel<Event> eventEntityModel = EntityModel.of(newEvent);


        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();
        // eventEntityModel.add(Link.of("http://localhost:8080/??"));  ❌ 와 같지만 타입 세이프티하지 않음!!
        eventEntityModel.add(linkTo(EventController.class).withRel("query-events"));
        eventEntityModel.add(selfLinkBuilder.withSelfRel());   // 👉 withSelfRel()를 사용해서 자기 자신 사용
        eventEntityModel.add(selfLinkBuilder.withRel("update-event"));
        // ✏️ 프로필 추가!
        eventEntityModel.add(Link.of("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(eventEntityModel);
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler pagedResourcesAssembler){
        Page<Event> page = this.eventRepository.findAll(pageable);
        var pagedResources  = pagedResourcesAssembler.toModel(page);
        return ResponseEntity.ok(pagedResources);
    }

    private EntityModel<ErrorDTO> makeErrorDTO(Errors errors){
        ErrorDTO errorDTO = ErrorDTO.builder().status(999).build();
        errors.getFieldErrors().forEach(item->{
            errorDTO.setField(item.getField());
            errorDTO.setMessage(item.getDefaultMessage());
        });
        EntityModel<ErrorDTO> errorModel = EntityModel.of(errorDTO);
        errorModel.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
        return  errorModel;
    }
}
