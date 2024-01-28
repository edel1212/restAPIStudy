package com.yoo.restAPI.events;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;


@Builder // Builder 어노테이션만 사용 시 아무것도 없는 생성자를 만들 수가 없는 문제가 있다.
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
/**
 * 지정한 값을 기준으로 entity간의 비교가 가능해짐
 * - 사용을 하지 않으면 모든 값을 기준으로 비교하는데 이떄 "상호 참조"떄문에 stackoverflow가 발생할 수 도 있음
 * - 원한다먄 Set 형태로도 여러개의 비교 값을 지정이 가능함
 *   - ex) ( of = {"id", "name"})
 * ✨ 여기서도 중요한건  stackoverflow가 발생하지 않게 EqualsAndHashCode에는
 *    다른 Entity를 참조하는 필드를 넣지 않느 것이다.
 * */
@EqualsAndHashCode( of = "id")
@Entity
// @Data  <<가 있지만 Entity에서는 사용하지말자 위에서 말한 EqualsAndHashCode를 모든 필드를 대상으로 만들기 떄문이다.
public class Event {
    // 식별자
    @Id @GeneratedValue
    private Integer id;

    // 이벤트 상태
    /**
     * 👉 @Enumerated을 지정해주자 그래야 String으로 들어감
     *    미사용시 enum의 순서로 저장되기에 순서가 바뀌면 위험해지기 떄문이다.
     * */
    @Enumerated(EnumType.STRING)
    private @Builder.Default EventStatus eventStatus = EventStatus.DRAFT;

    // 오프라인 구분
    private boolean offline;
    // 유료, 무료구분
    private boolean free;

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

    public void update() {
        // Update Free
        if(this.basePrice == 0 && this.maxPrice == 0){
            this.free = true;
        } else {
            this.free = false;
        }// if - else

        // Update Offline
        if( this.location == null || this.location.isBlank() ){
            this.offline = false;
        } else {
            this.offline = true;
        }
    }
}
