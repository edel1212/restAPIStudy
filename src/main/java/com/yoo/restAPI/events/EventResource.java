package com.yoo.restAPI.events;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.List;

//
public class EventResource extends EntityModel<Event> {
    public EventResource(Event event, Link... links) {
        super(event, List.of(links));
    }
}
