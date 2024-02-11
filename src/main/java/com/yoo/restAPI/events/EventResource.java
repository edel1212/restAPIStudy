package com.yoo.restAPI.events;

import lombok.Getter;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@Getter
public class EventResource extends EntityModel<Event> {
    public EventResource(Event event, Links links){
        super(event, links);
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }
}
