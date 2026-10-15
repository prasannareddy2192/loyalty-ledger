package com.prasanna.ledger.member;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members/{memberId}")
public class LedgerController {

    public record BalanceResponse(Long memberId, long balance) {
    }

    private final LedgerService ledger;

    public LedgerController(LedgerService ledger) {
        this.ledger = ledger;
    }

    @PostMapping("/earn")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse earn(@PathVariable Long memberId,
                                    @Valid @RequestBody TransactionRequest request) {
        return ledger.earn(memberId, request);
    }

    @PostMapping("/redeem")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse redeem(@PathVariable Long memberId,
                                      @Valid @RequestBody TransactionRequest request) {
        return ledger.redeem(memberId, request);
    }

    @GetMapping("/balance")
    public BalanceResponse balance(@PathVariable Long memberId) {
        return new BalanceResponse(memberId, ledger.balance(memberId));
    }

    @GetMapping("/transactions")
    public PagedModel<TransactionResponse> history(@PathVariable Long memberId, Pageable pageable) {
        return new PagedModel<>(ledger.history(memberId, pageable));
    }
}