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
        // 💬 주의 linkTo() 임포트를 WebFlux를 사용할 경우
        // Handler dispatch failed: java.lang.NoClassDefFoundError: reactor/util/context/ContextView 에러 발생 주의!
        index.add(linkTo(EventController.class).withRel("events"));
        return index;
      //  return RepresentationModel.of(linkTo(EventController.class).withRel("events"));
    }
}
