package com.prasanna.ledger.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateMemberRequest(
        @NotBlank String name,
        @Email @NotBlank String email
) {
}