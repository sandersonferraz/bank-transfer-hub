package com.transaction.transferservice.domain.model;

import java.time.Instant;

public record Entry(String accountId, EntryType type, Money amount, String reference, Instant instant) {

    public enum EntryType {DEBIT, CREDIT}

}
