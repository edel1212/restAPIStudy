# HATEOAS

## HATEOAS란?
```javascript
// ℹ️ Hypermedia As The Engine Of Application State의 약어로, RESTful API에서 Client <-> Server 상호 작용을 위해 HyperMedia를 사용하는 아키텍처 스타일이다.
//   - 응답에 따라 동적인 링크를 사용하여 API를 더 자기 기술적으로 만들어준다.
// ex)
{
  "id": 123,
  "name": "John Doe",
  "links": [
    {
      "rel": "self",
      "href": "/api/users/123"
    },
    {
      "rel": "edit",
      "href": "/api/users/123/edit"
    },
    {
      "rel": "delete",
      "href": "/api/users/123/delete"
    }
   ]
}
```

## 스프링 HATEOAS

- Docs
  - https://docs.spring.io/spring-hateoas/docs/current/reference/html/
- 기능
  - 리소스에 사용할 HREF 생성
    - `Controller` 또는 `Method`의 Path로 HREF 생성 
      - ex) `linkTo(MemberController.class)` 또는 `linkTo(methodOn(MemberController.class).getMemberList(파라미터)` 
    - 리소스 생성
        - 리소스: REL + HREF
    - 링크 찾아주는 기능
        - Traverson
        - LinkDiscoverers
- 의미
    - HREF (`hypertext reference`) : 링크 주소
    - REL (`Relation`) : rel 속성은 현재 문서와 외부 리소스 사이의 연관 관계를 명시
        - self : 자기자신
        - profile : 연결된 Docs
        - 자유 기입 : 해당 Rel은 해당 HREF에 맞는 연관 관계를 작성



## HATEOAS 응답 반환 방법
- `EntityModel` 사용 하여 `_link`(리소스) 정보 추가
- 이전 2.X 초반 SpringBoot에서는 따로 Class 생성 후 Resourse<T>를 상속 받아 구현 하였지만 버전이 바뀌면서 변경되었다.
    - ex) `public class EventResource extends Resource<Event>` 이후 super를 통한 생성자 필요
- 사용 방법

  ```java
  public class EventController(){
    @PostMapping
    public ResponseEntity createEvent(@RequestBody EventDTO eventDTO, Errors errors){


      // 💬 EntityModel<대상>을 통해 _Link를 생성 할 수 있는 HATEOAS 컨테이너 객체 생성
      EntityModel<Event> eventEntityModel = EntityModel.of(newEvent);

      // 👉 현재 사용하는 Class의 주소 정보를 읽어서 가져옴
      WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
      URI createdUri = selfLinkBuilder.toUri();

      // ⭐️ HATEOAS 컨테이너 객체 내 add이벤트를 통해 _link 생성
      /** _link.query-events */
      eventEntityModel.add(linkTo(EventController.class).withRel("query-events"));
      /** _link.self */
      eventEntityModel.add(selfLinkBuilder.withSelfRel());   // 👉 withSelfRel()를 사용해서 자기 자신 사용
      /** _link.self */
      eventEntityModel.add(selfLinkBuilder.withRel("update-event"));

      // ❌ eventEntityModel.add(Link.of("http://localhost:8080/??")); linkTo()를 사용하자 이건 타입 세이프티하지 않음!!

      // 👉 created()에 들어가면 header에 생성된다.
      // Headers = [Location:"http://localhost/api/events/1", Content-Type:"application/hal+json"]
      return ResponseEntity.created(createdUri).body(eventEntityModel);
    }
  }
  ```

## 프로필 생성 방법
  - 코드
  ```java
  public class MemberController {
    @PatchMapping(value = "/set-state", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<MemberStateRes>> setMemberState(@Valid @RequestBody MemberStateReq memberStateReq, BindingResult bindingResult){


      Link selfLink = Link.of( ServletUriComponentsBuilder
                                      .fromCurrentContextPath()
                                      .path("/docs/index.html")
                                      .fragment("여기다가 원하는 href 삽입")
                                      .toUriString() )
                              .withRel("profile");
        
      // Profile
      entityModel.add(selfLink);
      return ResponseEntity.ok().body(entityModel);
    }
  }
  ```

## PathVariable HREF 생성
- `slash(유동적인 값)`을 사용하면 쉽게 만들 수 있다
  - `linkTo(EventController.class).slash(newEvent.getId())` 


## Index 응답 만들기
- 첫 목록 페이지
- 주의사항
  - 👌 정상
      - `RepresentationModel`객체를 생성 후 `add()`메서드 사용
        - 응답 값 : `Body = {"_links":{"events":{"href":"http://localhost:8080/api/events"}}}` 
  - ❌ 원치 않은 결과
    - `RepresentationModel.of()`로 바로 사용 결과 `of()`로 인해 rel이 없다
      - 응답 값 :  `Body = {"href":"http://localhost:8080/api/events"}`  
- 사용 예시
  ```java
  @RestController
  public class IndexController {

      @GetMapping("/api")
      public RepresentationModel index(){
          // 👉 var는 임시 변수로 java 10부터 사용이 가능하다
          var index = new RepresentationModel();
          // ✨ linkTo Import 주의!!
          index.add(linkTo(EventController.class).withRel("events"));
          return index;
      }
  }
  ```


### 예외 처리
- 예외 발생 시 Index 페이지의 링크를 전달 해주면 된다.
  ```java
  public class EventController{
    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors){
        //  👉 예외 발생 시 처리
        if(errors.hasErrors()){
            ErrorDTO errorDTO = ErrorDTO.builder()
                    .field("fileName").status(999).message("Error!!").build();
            EntityModel<ErrorDTO> errorModel = EntityModel.of(errorDTO);
            errorModel.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
            return ResponseEntity.badRequest().body(errorModel);
        } // if
        // code ..
        return ResponseEntity.created(createdUri).body(eventEntityModel);
    }
  }
  ```

## Page 객체에 리소스 추가 - 페이징
- `PagedResourcesAssembler`을 사용하면 된다.
  - `assembler.toModel(Pageable 객체, EntityModel 변환 함수 )`를 파라미터로 주입
- 주의 사항
  - 현재 기본적인 페이징만을 사용하기에 구현 사항에따라 변경이 있을 수 있다.
    - where 조건이 없음
    - sort 조건이 한정적임
- 예시
  ```java
  public class EventController{
    @GetMapping                                     //  0 . PagedResourcesAssembler 파라미터로 주입
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler assembler){
        // 1 . 페이지 결과 생성
        Page<Event> page = this.eventRepository.findAll(pageable);
        // 2 . _link정보가 주입
        var pagedResources  = assembler.toModel(page, entity ->{
            EntityModel<Event> entityModel = EntityModel.of((Event) entity);
            entityModel.add(linkTo(EventController.class).slash(((Event) entity).getId()).withSelfRel());
            return entityModel;
        } );
        // 💬 profile 추가
        pagedResources.add(Link.of("/docs/index.html#resources-query-events").withRel("profile"));
        return ResponseEntity.ok(pagedResources);
    }
  }
  ```

## 사용간 주의 사항
- ### Import 주의 
  - HATEOS `_link`를 만들 떄 `linkTo()`를 사용할 떄 Import를 주의하자 - **삽질 1시간넘게 함**
    - ❌ 이거 아님 
      - `import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;`
    - 👍 이거로 해야함
      - `import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;`
  - Controller
    ```java
    import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

    @Controller
    @RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE )
    public class EventController {
        @PostMapping
        public ResponseEntity createEvent(@RequestBody Event event){
            /** Method에 path정보가 있을 경우 "methodOn()" 와 "해당함수명()" 를 사용해 추출해야 했음  */
            // URI createdUri = linkTo(methodOn(EventController.class).createEvent(event)).slash("{id}").toUri();

            URI createdUri = linkTo(EventController.class).slash("{id}").toUri();
            event.setId(999);
            return ResponseEntity.created(createdUri).body(event);
        }
    }
    ```
- ### URL Type Safety     
  - 메서드 내에 `Path` 값이 설정되어 있다면  `methodOn()` 함수를 사용해서 `_linke`를 만들어주자
    - 메서드의 `Path`가 바뀌어도 자동으로 바뀌게 되므로 Type Safety 해진다.
  - Controller
    ```java
    @RequestMapping(value = "/member", produces = MediaTypes.HAL_JSON_VALUE)
    @RequiredArgsConstructor
    @RestController
    @Log4j2
    public class MemberController {
        @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<EntityModel<JwtToken>> signIn(@Valid @RequestBody LoginReq loginReq, BindingResult bindingResult){
            /**
             * 👉 methodOn(사용 Controller).사용 메서드() ; << 지정으로 /member//sign-in 형식으로 _link가 만들어진다.
             * */
            entityModel.add(linkTo(methodOn(MemberController.class).signIn(loginReq, bindingResult)).withSelfRel());
            return ResponseEntity.ok().body(entityModel);
        }
    }
    ```
