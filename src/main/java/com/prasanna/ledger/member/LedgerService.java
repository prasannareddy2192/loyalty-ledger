package com.prasanna.ledger.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LedgerService {

    private final MemberRepository members;
    private final TransactionRepository transactions;

    public LedgerService(MemberRepository members, TransactionRepository transactions) {
        this.members = members;
        this.transactions = transactions;
    }

    @Transactional
    public TransactionResponse earn(Long memberId, TransactionRequest request) {
        return record(memberId, TransactionType.EARN, request);
    }

    @Transactional
    public TransactionResponse redeem(Long memberId, TransactionRequest request) {
        return record(memberId, TransactionType.REDEEM, request);
    }

    @Transactional(readOnly = true)
    public long balance(Long memberId) {
        findMember(memberId);
        return transactions.balance(memberId);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> history(Long memberId, Pageable pageable) {
        findMember(memberId);
        return transactions.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(TransactionResponse::from);
    }

    private TransactionResponse record(Long memberId, TransactionType type, TransactionRequest request) {
        Member member = members.findByIdForUpdate(memberId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        // Idempotency: if this key was already used, return the original result instead of saving again.
        var existing = transactions.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            Transaction t = existing.get();
            if (!t.getMember().getId().equals(memberId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency key already used for another member");
            }
            return TransactionResponse.from(t);
        }

        if (type == TransactionType.REDEEM && transactions.balance(memberId) < request.points()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient points");
        }

        Transaction saved = transactions.save(new Transaction(
                member, type, request.points(), request.idempotencyKey(), request.reference()));
        return TransactionResponse.from(saved);
    }

    private Member findMember(Long memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }
}