package com.transaction.transferservice.domain.model;

public record Account(String accountId, String holder, Money dailyLimit) {
    public boolean exceedsDailyLimit(Money amount) {
        return amount.amount().compareTo(dailyLimit.amount()) > 0;
    }
}
