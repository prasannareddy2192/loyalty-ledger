package com.prasanna.ledger.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock MemberRepository members;
    @Mock TransactionRepository transactions;
    @InjectMocks LedgerService ledger;

    private Member memberWithId(long id) {
        Member member = new Member("Sai", "sai@example.com");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    @Test
    void earnAddsTransaction() {
        when(members.findByIdForUpdate(1L)).thenReturn(Optional.of(memberWithId(1L)));
        when(transactions.findByIdempotencyKey("earn-1")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = ledger.earn(1L, new TransactionRequest(500, "order-1", "earn-1"));

        assertEquals(TransactionType.EARN, response.type());
        assertEquals(500, response.points());
        verify(transactions).save(any(Transaction.class));
    }

    @Test
    void redeemSucceedsWhenBalanceIsSufficient() {
        when(members.findByIdForUpdate(1L)).thenReturn(Optional.of(memberWithId(1L)));
        when(transactions.findByIdempotencyKey("redeem-1")).thenReturn(Optional.empty());
        when(transactions.balance(1L)).thenReturn(500L);
        when(transactions.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = ledger.redeem(1L, new TransactionRequest(200, "reward-1", "redeem-1"));

        assertEquals(TransactionType.REDEEM, response.type());
        verify(transactions).save(any(Transaction.class));
    }

    @Test
    void redeemIsRejectedWhenBalanceIsTooLow() {
        when(members.findByIdForUpdate(1L)).thenReturn(Optional.of(memberWithId(1L)));
        when(transactions.findByIdempotencyKey("redeem-2")).thenReturn(Optional.empty());
        when(transactions.balance(1L)).thenReturn(100L);

        var request = new TransactionRequest(400, "reward-2", "redeem-2");
        var ex = assertThrows(ResponseStatusException.class, () -> ledger.redeem(1L, request));

        assertEquals(409, ex.getStatusCode().value());
        verify(transactions, never()).save(any());
    }

    @Test
    void repeatedIdempotencyKeyReturnsOriginalWithoutSaving() {
        Member member = memberWithId(1L);
        Transaction original = new Transaction(member, TransactionType.EARN, 500, "earn-1", "order-1");
        when(members.findByIdForUpdate(1L)).thenReturn(Optional.of(member));
        when(transactions.findByIdempotencyKey("earn-1")).thenReturn(Optional.of(original));

        var response = ledger.earn(1L, new TransactionRequest(500, "order-1", "earn-1"));

        assertEquals(500, response.points());
        verify(transactions, never()).save(any());
    }

    @Test
    void balanceReturnsValueFromRepository() {
        when(members.findById(1L)).thenReturn(Optional.of(memberWithId(1L)));
        when(transactions.balance(1L)).thenReturn(300L);

        assertEquals(300L, ledger.balance(1L));
    }
}