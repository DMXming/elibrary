package com.dmx.elibrary.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dmx.elibrary.config.SecurityConfig;
import com.dmx.elibrary.domain.LoanStatus;
import com.dmx.elibrary.dto.LoanResponse;
import com.dmx.elibrary.service.LoanService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoanController.class)
@Import(SecurityConfig.class)
class LoanControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LoanService loanService;

    @Test
    void borrowReturns201WithLoan() throws Exception {
        LoanResponse response = new LoanResponse(5L, 1L, "Clean Code", 1L,
                Instant.parse("2026-10-26T10:00:00Z"), Instant.parse("2026-11-09T10:00:00Z"),
                null, LoanStatus.ACTIVE);
        given(loanService.borrow(1L, 1L)).willReturn(response);

        mockMvc.perform(post("/api/v1/loans")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void borrowWithoutHeaderReturns400UserMissing() throws Exception {
        mockMvc.perform(post("/api/v1/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_ID_MISSING"));
    }

    @Test
    void borrowWithMalformedHeaderReturns400UserMissing() throws Exception {
        mockMvc.perform(post("/api/v1/loans")
                        .header("X-User-Id", "abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_ID_MISSING"));
    }

    @Test
    void borrowWithNullBookIdReturns400ValidationFailed() throws Exception {
        mockMvc.perform(post("/api/v1/loans")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}
