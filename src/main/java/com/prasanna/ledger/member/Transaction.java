package com.prasanna.ledger.member;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private long points;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private String reference;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Transaction() {
        // required by JPA
    }

    public Transaction(Member member, TransactionType type, long points,
                       String idempotencyKey, String reference) {
        this.member = member;
        this.type = type;
        this.points = points;
        this.idempotencyKey = idempotencyKey;
        this.reference = reference;
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public TransactionType getType() { return type; }
    public long getPoints() { return points; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getReference() { return reference; }
    public Instant getCreatedAt() { return createdAt; }
}