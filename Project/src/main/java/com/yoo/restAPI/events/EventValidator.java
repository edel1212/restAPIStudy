package com.yoo.restAPI.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

@Component // Bean 등록
public class EventValidator {
    public void validate(EventDTO eventDTO, Errors errors){
        if(eventDTO.getBasePrice() > eventDTO.getMaxPrice()
            && eventDTO.getMaxPrice() > 0 ){
            // 👉 필드 에러
            errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
            errors.rejectValue("maxPrice", "wrongValue", "MaxPrice is wrong");
            // 👉 글로벌 에러
            errors.reject("globalError");
       }// if

        LocalDateTime eventEndTime =  eventDTO.getEndEventDateTime();
        if(eventEndTime.isBefore(eventDTO.getBeginEventDateTime())){
            errors.rejectValue("endEventDateTime", "wrongValue", " endEventDateTime is wrong");
        } // if

        // TODO 이런식이르 검증 로직을 만들어서 errors를 reject해준다.
    }
}
