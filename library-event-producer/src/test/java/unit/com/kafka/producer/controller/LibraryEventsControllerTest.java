package com.kafka.producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.producer.domain.Book;
import com.kafka.producer.domain.LibraryEvent;
import com.kafka.producer.eventproducer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc
class LibraryEventsControllerTest {

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void checkWhetherLibraryEventIsPostedSuccessfully() throws Exception {
        String requestBody = objectMapper.writeValueAsString(buildLibraryEvent());

        doNothing().when(libraryEventProducer).sendLibraryEventAnotherApproach(isA(LibraryEvent.class));

        mockMvc.perform(post("/v1/libraryevent")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        verify(libraryEventProducer, times(1)).sendLibraryEventAnotherApproach(any());
    }

    @Test
    void checkWhetherExceptionIsThrownForNullValues() throws Exception {

        String requestBody = objectMapper.writeValueAsString(buildLibraryEventWithNullValues());

        mockMvc.perform(post("/v1/libraryevent")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("book.bookAuthor-must not be blank,book.bookName-must not be blank"));
    }

    private LibraryEvent buildLibraryEventWithNullValues() {
        Book book = Book.builder().bookId(123)
                .bookAuthor(null)
                .bookName(null)
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book).build();
        return libraryEvent;
    }

    private LibraryEvent buildLibraryEvent() {
        Book book = Book.builder().bookId(123)
                .bookAuthor("Kalki")
                .bookName("PS")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book).build();
        return libraryEvent;
    }
}