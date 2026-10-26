package com.dmx.elibrary.dto;

import com.dmx.elibrary.domain.Loan;
import com.dmx.elibrary.domain.LoanStatus;
import java.time.Instant;

public record LoanResponse(Long id, Long bookId, String bookTitle, Long patronId,
        Instant borrowedAt, Instant dueAt, Instant returnedAt, LoanStatus status) {

    public static LoanResponse from(Loan loan, Instant now) {
        return new LoanResponse(loan.getId(), loan.getBook().getId(), loan.getBook().getTitle(),
                loan.getPatron().getId(), loan.getBorrowedAt(), loan.getDueAt(), loan.getReturnedAt(),
                loan.status(now));
    }
}
