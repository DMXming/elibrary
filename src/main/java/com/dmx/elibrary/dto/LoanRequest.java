package com.dmx.elibrary.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoanRequest(
        // Explicit messages pin the API's error detail to English regardless of
        // the JVM's default locale (the defaults are localized).
        @NotNull(message = "must not be null")
        @Positive(message = "must be positive")
        Long bookId) {
}
