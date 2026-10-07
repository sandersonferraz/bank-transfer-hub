package com.transaction.transferservice.domain.model;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {
    private static final String BRL = "BRL";

    public Money {
        if (amount == null || amount.signum() < 0) throw new IllegalArgumentException("Amount cannot be negative");
        if (currency == null) throw new IllegalArgumentException("Currency cannot be null");
        if (!currency.equals(BRL)) throw new IllegalArgumentException("Supported currency " + BRL);
    }

    public Money sum(Money other) {
        if (other == null) throw new NullPointerException("Amount cannot be null");
        if (!currency.equals(other.currency())) throw new IllegalArgumentException("Currency other not supported");
        return new Money(amount.add(other.amount), other.currency);
    }
}
