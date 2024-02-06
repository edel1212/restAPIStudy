package com.yoo.restAPI.index;

import com.yoo.restAPI.events.Event;
import com.yoo.restAPI.events.EventController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;

@RestController
public class IndexController {

    @GetMapping("/api")
    public ResponseEntity index(){
        return ResponseEntity.ok(EntityModel.of(linkTo(EventController.class).withRel("events")));
    }
}
