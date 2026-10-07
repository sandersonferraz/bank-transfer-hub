package com.transaction.transferservice.domain.model;

import com.transaction.transferservice.exception.InsufficientBalanceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.transaction.transferservice.domain.model.Entry.EntryType.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Ledger tests")
class LedgerTests {

    private static final Instant NOW = Instant.parse("2018-01-01T00:00:00.00Z");
    private static final String ACCOUNT_ID = "account-1";

    private static Money brl(String amount) {
        return new Money(new BigDecimal(amount), "BRL");
    }

    private static Ledger base() {
        Ledger ledger = new Ledger();
        ledger.credit(ACCOUNT_ID, brl("100.00"), "initial-credit", NOW);
        return ledger;
    }

    @Nested
    @DisplayName("initial state")
    class InitialState {

        @Test
        @DisplayName("Should have zero balance for a new account")
        void shouldHaveZeroBalanceForNewAccount() {
            Ledger ledger = new Ledger();
            assertThat(ledger.balance(ACCOUNT_ID)).isEqualTo(brl("0"));

        }

        @Test
        @DisplayName("Should have no entries for a new account")
        void shouldHaveNoEntriesForNewAccount() {
            Ledger ledger = new Ledger();
            assertThat(ledger.entries(ACCOUNT_ID)).isEmpty();
        }

        @Test
        @DisplayName("Should return zero balance for an unknown account")
        void shouldReturnZeroBalanceForUnknownAccount() {
            Ledger ledger = base();
            assertThat(ledger.balance("unknown-account")).isEqualTo(brl("0"));
        }
    }

    @Nested
    @DisplayName("credit")
    class Credit {

        @Test
        @DisplayName("Should add a credit Entry")
        void shouldAddCreditEntry() {

            Ledger ledger = new Ledger();

            ledger.credit(
                    ACCOUNT_ID,
                    brl("100.00"),
                    "pix-001",
                    NOW
            );

            assertThat(ledger.entries(ACCOUNT_ID))
                    .hasSize(1);

            Entry Entry =
                    ledger.entries(ACCOUNT_ID).getFirst();

            assertThat(Entry.accountId())
                    .isEqualTo(ACCOUNT_ID);

            assertThat(Entry.type())
                    .isEqualTo(CREDIT);

            assertThat(Entry.amount())
                    .isEqualTo(brl("100.00"));

            assertThat(Entry.reference())
                    .isEqualTo("pix-001");

            assertThat(Entry.instant())
                    .isEqualTo(NOW);
        }

        @Test
        @DisplayName("Should increase account balance")
        void shouldIncreaseAccountBalance() {

            Ledger ledger = new Ledger();

            ledger.credit(
                    ACCOUNT_ID,
                    brl("100.00"),
                    "pix-001",
                    NOW
            );

            assertThat(ledger.balance(ACCOUNT_ID))
                    .isEqualTo(brl("100.00"));
        }

        @Test
        @DisplayName("Should accumulate multiple credits")
        void shouldAccumulateMultipleCredits() {

            Ledger ledger = new Ledger();

            ledger.credit(
                    ACCOUNT_ID,
                    brl("100.00"),
                    "pix-001",
                    NOW
            );

            ledger.credit(
                    ACCOUNT_ID,
                    brl("50.00"),
                    "pix-002",
                    NOW.plusSeconds(1)
            );

            assertThat(ledger.balance(ACCOUNT_ID))
                    .isEqualTo(brl("150.00"));
        }
    }

    @Nested
    @DisplayName("debit")
    class Debit {

        @Test
        @DisplayName("Should add a debit Entry when balance is sufficient")
        void shouldAddDebitEntryWhenBalanceIsSufficient() {

            Ledger ledger = base();

            ledger.debit(
                    ACCOUNT_ID,
                    brl("40.00"),
                    "purchase-001",
                    NOW.plusSeconds(1)
            );

            assertThat(ledger.entries(ACCOUNT_ID))
                    .hasSize(2);

            Entry Entry =
                    ledger.entries(ACCOUNT_ID).get(1);

            assertThat(Entry.type())
                    .isEqualTo(DEBIT);

            assertThat(Entry.amount())
                    .isEqualTo(brl("40.00"));

            assertThat(Entry.reference())
                    .isEqualTo("purchase-001");
        }

        @Test
        @DisplayName("Should decrease account balance")
        void shouldDecreaseAccountBalance() {

            Ledger ledger = base();

            ledger.debit(
                    ACCOUNT_ID,
                    brl("40.00"),
                    "purchase-001",
                    NOW.plusSeconds(1)
            );

            assertThat(ledger.balance(ACCOUNT_ID))
                    .isEqualTo(brl("60.00"));
        }

        @Test
        @DisplayName("Should allow debit equal to balance")
        void shouldAllowDebitEqualToBalance() {

            Ledger ledger = base();

            ledger.debit(
                    ACCOUNT_ID,
                    brl("100.00"),
                    "purchase-001",
                    NOW.plusSeconds(1)
            );

            assertThat(ledger.balance(ACCOUNT_ID))
                    .isEqualTo(brl("0.00"));
        }

        @Test
        @DisplayName("Should reject debit greater than balance")
        void shouldRejectDebitGreaterThanBalance() {

            Ledger ledger = base();

            assertThatThrownBy(() ->
                    ledger.debit(
                            ACCOUNT_ID,
                            brl("100.01"),
                            "purchase-001",
                            NOW.plusSeconds(1)
                    )
            )
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        @DisplayName("Should include account id in insufficient balance exception")
        void shouldIncludeAccountIdInInsufficientBalanceException() {

            Ledger ledger = base();

            assertThatThrownBy(() ->
                    ledger.debit(
                            ACCOUNT_ID,
                            brl("100.01"),
                            "purchase-001",
                            NOW.plusSeconds(1)
                    )
            )
                    .isInstanceOfSatisfying(
                            InsufficientBalanceException.class,
                            exception ->
                                    assertThat(exception.getMessage())
                                            .contains(ACCOUNT_ID)
                    );
        }

        @Test
        @DisplayName("Should not add Entry when debit is rejected")
        void shouldNotAddEntryWhenDebitIsRejected() {

            Ledger ledger = base();

            assertThatThrownBy(() ->
                    ledger.debit(
                            ACCOUNT_ID,
                            brl("100.01"),
                            "purchase-001",
                            NOW.plusSeconds(1)
                    )
            )
                    .isInstanceOf(InsufficientBalanceException.class);

            assertThat(ledger.entries(ACCOUNT_ID))
                    .hasSize(1);

            assertThat(ledger.balance(ACCOUNT_ID))
                    .isEqualTo(brl("100.00"));
        }
    }

    @Nested
    @DisplayName("balance projection")
    class BalanceProjection {

        @Test
        @DisplayName("Should calculate balance from credits and debits")
        void shouldCalculateBalanceFromCreditsAndDebits() {

            Ledger ledger = new Ledger();

            ledger.credit(
                    ACCOUNT_ID,
                    brl("500.00"),
                    "credit-001",
                    NOW
            );

            ledger.debit(
                    ACCOUNT_ID,
                    brl("150.00"),
                    "debit-001",
                    NOW.plusSeconds(1)
            );

            ledger.credit(
                    ACCOUNT_ID,
                    brl("200.00"),
                    "credit-002",
                    NOW.plusSeconds(2)
            );

            ledger.debit(
                    ACCOUNT_ID,
                    brl("50.00"),
                    "debit-002",
                    NOW.plusSeconds(3)
            );

            assertThat(ledger.balance(ACCOUNT_ID))
                    .isEqualTo(brl("500.00"));
        }

        @Test
        @DisplayName("Should calculate balance independently for each account")
        void shouldCalculateBalanceIndependentlyForEachAccount() {

            Ledger ledger = new Ledger();

            ledger.credit(
                    "account-1",
                    brl("100.00"),
                    "credit-001",
                    NOW
            );

            ledger.credit(
                    "account-2",
                    brl("500.00"),
                    "credit-002",
                    NOW
            );

            ledger.debit(
                    "account-2",
                    brl("100.00"),
                    "debit-001",
                    NOW.plusSeconds(1)
            );

            assertThat(ledger.balance("account-1"))
                    .isEqualTo(brl("100.00"));

            assertThat(ledger.balance("account-2"))
                    .isEqualTo(brl("400.00"));
        }

        @Test
        @DisplayName("Should ignore entries belonging to other accounts")
        void shouldIgnoreEntriesBelongingToOtherAccounts() {

            Ledger ledger = new Ledger();

            ledger.credit(
                    "account-1",
                    brl("100.00"),
                    "credit-001",
                    NOW
            );

            ledger.credit(
                    "account-2",
                    brl("900.00"),
                    "credit-002",
                    NOW
            );

            assertThat(ledger.balance("account-1"))
                    .isEqualTo(brl("100.00"));
        }
    }

    @Nested
    @DisplayName("Entries")
    class Entries {

        @Test
        @DisplayName("Should return all Entries for account")
        void shouldReturnAllEntriesForAccount() {
            Ledger ledger = base();
            ledger.debit(
                    ACCOUNT_ID,
                    brl("20.00"),
                    "debit-001",
                    NOW.plusSeconds(1)
            );
            ledger.credit(
                    ACCOUNT_ID,
                    brl("50.00"),
                    "credit-001",
                    NOW.plusSeconds(2)
            );
            assertThat(ledger.entries(ACCOUNT_ID))
                    .hasSize(3);
        }

        @Test
        @DisplayName("Should return only Entries belonging to requested account")
        void shouldReturnOnlyEntriesBelongingToRequestedAccount() {
            Ledger ledger = new Ledger();
            ledger.credit(
                    "account-1",
                    brl("100.00"),
                    "credit-001",
                    NOW
            );
            ledger.credit(
                    "account-2",
                    brl("200.00"),
                    "credit-002",
                    NOW
            );
            assertThat(ledger.entries("account-1"))
                    .hasSize(1)
                    .allMatch(
                            Entry ->
                                    Entry.accountId()
                                            .equals("account-1")
                    );
        }

        @Test
        @DisplayName("Should preserve Entry order")
        void shouldPreserveEntryOrder() {
            Ledger ledger = new Ledger();
            ledger.credit(
                    ACCOUNT_ID,
                    brl("100.00"),
                    "first",
                    NOW
            );
            ledger.credit(
                    ACCOUNT_ID,
                    brl("200.00"),
                    "second",
                    NOW.plusSeconds(1)
            );
            List<Entry> entries = ledger.entries(ACCOUNT_ID);
            assertThat(entries)
                    .extracting(Entry::reference)
                    .containsExactly("first", "second"
                    );
        }

        @Test
        @DisplayName("Should return empty list for account without entries")
        void shouldReturnEmptyListForAccountWithoutEntries() {
            Ledger ledger = base();
            assertThat(ledger.entries("unknown-account")).isEmpty();
        }
    }

}
