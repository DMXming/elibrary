package com.dmx.elibrary.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dmx.elibrary.config.SecurityConfig;
import com.dmx.elibrary.domain.LoanStatus;
import com.dmx.elibrary.dto.LoanResponse;
import com.dmx.elibrary.exception.BusinessException;
import com.dmx.elibrary.exception.ErrorCode;
import com.dmx.elibrary.service.LoanService;
import java.time.Instant;
import java.util.List;
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

    @Test
    void returnLoanReturns200() throws Exception {
        LoanResponse response = new LoanResponse(1L, 1L, "Clean Code", 1L,
                Instant.parse("2026-10-26T10:00:00Z"), Instant.parse("2026-11-09T10:00:00Z"),
                Instant.parse("2026-10-27T10:00:00Z"), LoanStatus.RETURNED);
        given(loanService.returnLoan(1L, 1L)).willReturn(response);

        mockMvc.perform(post("/api/v1/loans/1/return")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }

    @Test
    void returnOtherPatronsLoanReturns404() throws Exception {
        given(loanService.returnLoan(2L, 1L))
                .willThrow(new BusinessException(ErrorCode.LOAN_NOT_FOUND));

        mockMvc.perform(post("/api/v1/loans/1/return")
                        .header("X-User-Id", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("LOAN_NOT_FOUND"));
    }

    @Test
    void myLoansReturns200WithArray() throws Exception {
        LoanResponse response = new LoanResponse(1L, 1L, "Clean Code", 1L,
                Instant.parse("2026-10-26T10:00:00Z"), Instant.parse("2026-11-09T10:00:00Z"),
                null, LoanStatus.ACTIVE);
        given(loanService.listCurrentLoans(1L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/me/loans")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(1));
    }
}
