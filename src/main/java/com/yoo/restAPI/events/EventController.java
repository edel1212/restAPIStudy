package com.yoo.restAPI.events;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;


@RestController
public class EventController {

    @PostMapping("/api/events")
    public ResponseEntity createEvent(){

         //URI uri = linkTo(methodOn(EventController.class).createEvent()).slash("{id}");

        URI createdUri = URI.create("");

        return ResponseEntity.created(createdUri).build();
    }

}
