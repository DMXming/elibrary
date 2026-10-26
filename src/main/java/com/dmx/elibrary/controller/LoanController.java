package com.dmx.elibrary.controller;

import com.dmx.elibrary.dto.LoanRequest;
import com.dmx.elibrary.dto.LoanResponse;
import com.dmx.elibrary.exception.BusinessException;
import com.dmx.elibrary.exception.ErrorCode;
import com.dmx.elibrary.service.LoanService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/loans")
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse borrow(@RequestHeader(name = "X-User-Id", required = false) String userId,
            @Valid @RequestBody LoanRequest request) {
        return loanService.borrow(parseUserId(userId), request.bookId());
    }

    @PostMapping("/loans/{loanId}/return")
    public LoanResponse returnLoan(@PathVariable Long loanId,
            @RequestHeader(name = "X-User-Id", required = false) String userId) {
        return loanService.returnLoan(parseUserId(userId), loanId);
    }

    @GetMapping("/me/loans")
    public List<LoanResponse> myLoans(@RequestHeader(name = "X-User-Id", required = false) String userId) {
        return loanService.listCurrentLoans(parseUserId(userId));
    }

    /**
     * The gateway injects {@code X-User-Id} after authenticating the caller;
     * a missing or non-numeric value is a client contract violation.
     */
    private static long parseUserId(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(ErrorCode.USER_ID_MISSING);
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.USER_ID_MISSING);
        }
    }
}
