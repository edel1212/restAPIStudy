package com.yoo.restAPI.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

@Component // Bean 등록
public class EventValidator {
    public void validate(EventDTO eventDTO, Errors errors){
        if(eventDTO.getBasePrice() > eventDTO.getMaxPrice()
            && eventDTO.getMaxPrice() > 0 ){
            errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
            errors.rejectValue("maxPrice", "wrongValue", "MaxPrice is wrong");
        }

        LocalDateTime eventEndTime =  eventDTO.getEndEventDateTime();
        if(eventEndTime.isBefore(eventDTO.getBeginEventDateTime())){
            errors.rejectValue("endEventDateTime", "wrongValue", " endEventDateTime is wrong");
        }

        // TODO 이런식이르 검증 로직을 만들어서 errors를 reject해준다.
    }
}
