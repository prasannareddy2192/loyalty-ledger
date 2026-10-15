package com.prasanna.ledger.member;

import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long memberId,
        TransactionType type,
        long points,
        String reference,
        String idempotencyKey,
        Instant createdAt
) {
    static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getMember().getId(),
                t.getType(),
                t.getPoints(),
                t.getReference(),
                t.getIdempotencyKey(),
                t.getCreatedAt());
    }
}