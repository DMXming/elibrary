package com.dmx.elibrary.service;

import com.dmx.elibrary.dto.LoanResponse;

public interface LoanService {

    /**
     * Borrows a copy of the book for the patron identified by the gateway.
     *
     * @param patronId the authenticated patron (from the {@code X-User-Id} header)
     * @param bookId   the book to borrow
     */
    LoanResponse borrow(Long patronId, Long bookId);
}
