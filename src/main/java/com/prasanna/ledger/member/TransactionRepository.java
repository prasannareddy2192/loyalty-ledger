package com.prasanna.ledger.member;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Page<Transaction> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN t.type = com.prasanna.ledger.member.TransactionType.EARN
                                 THEN t.points ELSE -t.points END), 0)
        FROM Transaction t WHERE t.member.id = :memberId
        """)
    long balance(@Param("memberId") Long memberId);
}