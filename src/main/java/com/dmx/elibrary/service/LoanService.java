package com.dmx.elibrary.service;

import com.dmx.elibrary.dto.LoanResponse;
import java.util.List;

public interface LoanService {

    /**
     * Borrows a copy of the book for the patron identified by the gateway.
     *
     * @param patronId the authenticated patron (from the {@code X-User-Id} header)
     * @param bookId   the book to borrow
     */
    LoanResponse borrow(Long patronId, Long bookId);

    /**
     * Returns one of the patron's active loans and restores the copy.
     * Throws {@code LOAN_NOT_FOUND} when the loan does not exist or belongs
     * to another patron, so ownership is never revealed.
     *
     * @param patronId the authenticated patron (from the {@code X-User-Id} header)
     * @param loanId   the loan to return
     */
    LoanResponse returnLoan(Long patronId, Long loanId);

    /** All of the patron's currently active loans, soonest due date first. */
    List<LoanResponse> listCurrentLoans(Long patronId);
}
