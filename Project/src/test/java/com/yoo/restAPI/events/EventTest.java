package com.yoo.restAPI.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    //@Test
    void builder() {
        Event event = Event.builder()
                .name("Builder Pattern을 사용해서 만듬")
                .build();
        assertThat(event).isNotNull();
    }

    //@Test
    void havaBean() {
        // Given
        String name = "Java Bean  스팩에 맞게 사용 하여 만듬";
        String description = "Spring Boot Project Test Code!";

        // When
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        // Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);

    }

   // @Test
    void testFree() {
        // Given
        Event event = Event.builder()
                .basePrice(0)
                .maxPrice(0)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isTrue();

        /**************/

        // Given
        event = Event.builder()
                .basePrice(1000)
                .maxPrice(0)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isFalse();
    }

    //@Test
    void testOffline() {
        // Given
        Event event = Event.builder()
                .location("의정부")
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isTrue();

        // Given
        event = Event.builder().build();

        // When
        event.update();

        // Then
        assertThat(event.isOffline()).isFalse();
    }

    /*****************/
    // Refactoring
    @DisplayName("파리머터를 통해 여러번 테스트가 가능")
   // @ParameterizedTest //👉 해당 어노테이션을 사용하면 여러개의 테스트 케이스를 한번에 실행 가능
    @MethodSource("provideFree")
    public void testFree_Refactoring(int basePrice, int maxPrice, boolean expected) {
        // Given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        // When
        event.update();

        // Then
        assertThat(event.isFree()).isEqualTo(expected);

    }

    // 👉 해당 Stream의 순서대로 값이 들어간다.
    private static Stream<Arguments> provideFree() {
        return Stream.of(
                Arguments.of(0, 0, true),
                Arguments.of(1_000, 0, true)
        );
    }
}