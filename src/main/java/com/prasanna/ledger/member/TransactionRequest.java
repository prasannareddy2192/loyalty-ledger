package com.prasanna.ledger.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
        @Positive long points,
        String reference,
        @NotBlank String idempotencyKey
) {
}