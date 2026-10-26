package com.dmx.elibrary.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoanRequest(
        @NotNull @Positive Long bookId) {
}
