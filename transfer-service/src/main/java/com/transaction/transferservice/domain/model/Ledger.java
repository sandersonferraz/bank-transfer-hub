package com.transaction.transferservice.domain.model;


import com.transaction.transferservice.exception.InsufficientBalanceException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ledger {

    private final List<Entry> entries = new ArrayList<>();

    public void debit(String accountId, Money amount, String reference, Instant instant) {
        if (balance(accountId).amount().compareTo(amount.amount()) < 0) {
            throw new InsufficientBalanceException(accountId);
        }
        entries.add(new Entry(accountId, Entry.EntryType.DEBIT, amount, reference, instant));
    }

    public void credit(String accountId, Money amount, String reference, Instant instant) {
        entries.add(new Entry(accountId, Entry.EntryType.CREDIT, amount, reference, instant));
    }

    public Money balance(String accountId) {
        BigDecimal total = BigDecimal.ZERO;
        for (Entry transaction : entries) {
            if (!transaction.accountId().equals(accountId)) {
                continue;
            }
            total = transaction.type() == Entry.EntryType.CREDIT
                    ? total.add(transaction.amount().amount())
                    : total.subtract(transaction.amount().amount());
        }

        return new Money(total, "BRL");
    }

    public List<Entry> entries(String accountId) {
        return entries.stream()
                .filter(transaction ->
                        transaction.accountId().equals(accountId))
                .toList();
    }
}
