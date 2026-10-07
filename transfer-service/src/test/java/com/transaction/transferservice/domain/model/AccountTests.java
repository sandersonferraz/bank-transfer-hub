package com.transaction.transferservice.domain.model;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Account tests")
public class AccountTests {

    private static Money brl(String amount) { return new Money(new BigDecimal(amount), "BRL");}
    private static Account base() {
        return new Account("account-1", "Jhon Wick", brl("1000.00"));
    }

    @Nested
    @DisplayName("Construction and accessors")
    class ConstructionAndAccessors {

        @Test
        @DisplayName("Should store id and expose it through accessor")
        void shouldStoreIdAndExposeItThroughAccessor() {
            Account account = base();
            Assertions.assertThat(account.accountId()).isEqualTo("account-1");
        }

        @Test
        @DisplayName("Should store holder and expose it through accessor")
        void shouldStoreHolderAndExposeItThroughAccessor() {
            Account account = base();
            assertThat(account.holder()).isEqualTo("Jhon Wick");
        }

        @Test
        @DisplayName("Should store daily limit and expose it through accessor")
        void shouldStoreDailyLimitAndExposeItThroughAccessor() {
            Money dailyLimit = brl("1000.00");
            Account account = new Account("account-1", "John Wick", dailyLimit);
            assertThat(account.dailyLimit()).isSameAs(dailyLimit);
        }
    }

    @Nested
    @DisplayName("exceeds daily limit")
    class ExceedsDailyLimit {

        @Test
        @DisplayName("Should return true when amount exceeds daily limit")
        void shouldReturnTrueWhenAmountExceedsDailyLimit() {
            Account account = new Account("account-1", "John Wick", brl("1000.00"));
            assertThat(account.exceedsDailyLimit(brl("1000.01"))).isTrue();
        }

        @Test
        @DisplayName("Should return false when amount equals daily limit")
        void shouldReturnFalseWhenAmountEqualsDailyLimit() {
            Account account = new Account("account-1", "John Wick", brl("1000.00"));
            assertThat(account.exceedsDailyLimit(brl("1000.00"))).isFalse();
        }

        @Test
        @DisplayName("Should return false when amount is below daily limit")
        void shouldReturnFalseWhenAmountIsBelowDailyLimit() {
            Account account = new Account("account-1", "John Wick", brl("1000.00"));
            assertThat(account.exceedsDailyLimit(brl("999.99"))).isFalse();
        }

        @Test
        @DisplayName("Should return false when daily limit and amount are zero")
        void shouldReturnFalseWhenDailyLimitAndAmountAreZero() {
            Account account = new Account("account-1", "John Wick", brl("0.00"));
            assertThat(account.exceedsDailyLimit(brl("0.00"))).isFalse();
        }

        @Test
        @DisplayName("Should return true when daily limit is zero and amount is positive")
        void shouldReturnTrueWhenDailyLimitIsZeroAndAmountIsPositive() {
            Account account = new Account("account-1", "John Wick", brl("0.00"));
            assertThat(account.exceedsDailyLimit(brl("0.01")))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("exceeds daily limit with multiple values")
    class ExceedsDailyLimitParameterized {
        static Stream<Arguments> amountsForDailyLimit() {
            return Stream.of(
                    Arguments.of("0.00", false),
                    Arguments.of("1.00", false),
                    Arguments.of("500.00", false),
                    Arguments.of("999.99", false),
                    Arguments.of("1000.00", false),
                    Arguments.of("1000.01", true),
                    Arguments.of("1500.00", true),
                    Arguments.of("10000.00", true)
            );
        }

        @ParameterizedTest(name = "amount {0} → exceeds daily limit = {1}")
        @MethodSource("amountsForDailyLimit")
        @DisplayName("Should correctly determine whether amount exceeds daily limit")
        void shouldCorrectlyDetermineWhetherAmountExceedsDailyLimit(String amount, boolean expected) {
            Account account = base();
            assertThat(account.exceedsDailyLimit(brl(amount)))
                    .isEqualTo(expected);
        }
    }

}
