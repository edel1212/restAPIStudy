package com.yoo.restAPI.index;

import com.yoo.restAPI.events.EventController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@RestController
public class IndexController {

    @GetMapping("/api")
    public RepresentationModel index(){
        // 👉 var는 임시 변수로 java 10부터 사용이 가능하다
        var index = new RepresentationModel();
        index.add(linkTo(EventController.class).withRel("events"));
        return index;
    }
}
