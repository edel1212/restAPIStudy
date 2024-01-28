package com.yoo.restAPI.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    void builder() {
        Event event = Event.builder()
                .name("Builder Pattern을 사용해서 만듬")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
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

    @Test
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

    @Test
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
}