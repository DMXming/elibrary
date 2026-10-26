package com.dmx.elibrary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exercises the five spec features against the real stack: Flyway migrations,
 * seed data, repositories, services, controllers, security config, and error
 * handling. Each test rolls back, so they stay independent of run order.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ELibraryEndToEndTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void fullBorrowReturnLifecycle() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));

        MvcResult borrow = mockMvc.perform(post("/api/v1/loans")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        int loanId = JsonPath.read(borrow.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/v1/me/loans")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookId").value(1));

        mockMvc.perform(post("/api/v1/loans/{loanId}/return", loanId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnedAt").isNotEmpty())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        mockMvc.perform(get("/api/v1/me/loans")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void secondPatronBorrowingLastCopyGets409() throws Exception {
        // Book 3 is seeded with a single copy.
        mockMvc.perform(post("/api/v1/loans")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":3}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/loans")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NO_COPIES_AVAILABLE"));
    }

    @Test
    void unknownRouteReturns404WithCode() throws Exception {
        mockMvc.perform(get("/api/v1/definitely-not-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void validationErrorsUseExactEnglishDetailRegardlessOfMachineLocale() throws Exception {
        // The exact-string pin also catches locale drift: on a non-English JVM the
        // default message comes out localized, on an English JVM it reads
        // "must be greater than 0" — neither equals the pin.
        mockMvc.perform(post("/api/v1/loans")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.detail").value("bookId: must be positive"));
    }

    @Test
    void unknownPatronAndUnknownBookReturn404ProblemDetails() throws Exception {
        mockMvc.perform(post("/api/v1/loans")
                        .header("X-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PATRON_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/books/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }
}
