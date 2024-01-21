package com.yoo.restAPI.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EventDTO {
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    // (optional) 이게 없으면 온라인 모임
    private String location;
    // (optional)
    private int basePrice;
    // (optional)
    private int maxPrice;
    private int limitOfEnrollment;
}
