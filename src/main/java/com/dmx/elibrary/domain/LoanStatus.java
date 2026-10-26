package com.dmx.elibrary.domain;

/**
 * Derived from {@code returnedAt} and {@code dueAt}; never stored,
 * so the status can never disagree with the timestamps.
 */
public enum LoanStatus {
    ACTIVE,
    OVERDUE,
    RETURNED
}
