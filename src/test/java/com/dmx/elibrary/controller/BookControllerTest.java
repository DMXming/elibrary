package com.dmx.elibrary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dmx.elibrary.config.SecurityConfig;
import com.dmx.elibrary.dto.BookDetailResponse;
import com.dmx.elibrary.dto.BookSummaryResponse;
import com.dmx.elibrary.dto.PageResponse;
import com.dmx.elibrary.exception.BusinessException;
import com.dmx.elibrary.exception.ErrorCode;
import com.dmx.elibrary.service.BookService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @Test
    void listBooksReturns200WithPageShape() throws Exception {
        PageResponse<BookSummaryResponse> page = new PageResponse<>(
                List.of(new BookSummaryResponse(1L, "Clean Code", "Robert C. Martin", "Software Engineering", 2)),
                0, 20, 1, 1);
        given(bookService.search(eq("clean"), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/v1/books").param("q", "clean").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.content[0].availableCopies").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getBookReturns200() throws Exception {
        BookDetailResponse detail = new BookDetailResponse(1L, "Clean Code", "Robert C. Martin", "978-0132350884",
                "Prentice Hall", LocalDate.of(2008, 8, 11), "Software Engineering", "A handbook.", 2, 2);
        given(bookService.getDetail(1L)).willReturn(detail);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("978-0132350884"));
    }

    @Test
    void getMissingBookReturns404ProblemDetail() throws Exception {
        given(bookService.getDetail(999L)).willThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        mockMvc.perform(get("/api/v1/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }
}
