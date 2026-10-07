package com.transaction.transferservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money tests")
class MoneyTests {

    private static final String BRL = "BRL";

    private static Money brl(String amount) {
        return new Money(new BigDecimal(amount), BRL);
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @ParameterizedTest(name = "accepts valid amount {0}")
        @ValueSource(strings = {"0", "0.00", "0.01", "1", "10.50", "999999999999.99", "0.0000001"})
        @DisplayName("Should create with valid amount")
        void shouldCreateWithValidAmount(String amount) {
            Money money = brl(amount);
            assertThat(money.amount()).isEqualTo(amount);
            assertThat(money.currency()).isEqualTo(BRL);

        }


        @Test
        @DisplayName("Should accept zero amount")
        void shouldAcceptsZeroAmount() {
            assertThatCode(() -> new Money(BigDecimal.ZERO.negate(), BRL)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept negative zero (signum == 0)")
        void shouldAcceptsNegativeZero() {
            assertThatCode(() -> new Money(BigDecimal.ZERO, BRL)).doesNotThrowAnyException();
        }

        @DisplayName("Should reject negative amount")
        @ParameterizedTest(name = "Should reject negative amount {0}")
        @ValueSource(strings = {"-0.01", "-1", "-100.50", "-0.0000001"})
        void shouldRejectsNegativeAmount(String amount) {
            assertThatIllegalArgumentException().isThrownBy(() -> brl(amount))
                    .withMessage("Amount cannot be negative");


        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should reject null amount")
        void shouldRejectNullAmount(BigDecimal amount) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Money(amount, BRL))
                    .withMessage("Amount cannot be negative");
        }


        @ParameterizedTest
        @NullSource
        @DisplayName("Should reject null currency")
        void shouldRejectNullCurrency(String currency) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Money(BigDecimal.TEN, currency))
                    .withMessage("Currency cannot be null");
        }

        @ParameterizedTest(name = "rejects unsupported currency \"{0}\"")
        @ValueSource(strings = {"USD", "EUR", "brl", "Brl", " BRL", "BRL ", "", " "})
        @DisplayName("Should reject unsupported currency")
        void shouldRejectUnsupportedCurrency(String currency) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Money(BigDecimal.TEN, currency))
                    .withMessage("Supported currency BRL");
        }

        @Test
        @DisplayName("Should validate amount before currency when both are invalid")
        void shouldReportAmountErrorFirstWhenBothInvalid() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Money(new BigDecimal("-1"), "USD"))
                    .withMessage("Amount cannot be negative");
        }

    }

    @Nested
    @DisplayName("Sum")
    class Sum {

        @ParameterizedTest(name = "{0} + {1} = {2}")
        @CsvSource({
                "10.00,  5.00,  15.00",
                "0.10,   0.20,  0.30",
                "0,      0,     0",
                "0,      7.50,  7.50",
                "7.50,   0,     7.50",
                "0.01,   0.01,  0.02",
                "999999999999.99, 0.01, 1000000000000.00"
        })
        @DisplayName("Should sum amounts")
        void shouldSumAmounts(String a, String b, String expected) {
            Money result = brl(a).sum(brl(b));
            assertThat(result.amount()).isEqualByComparingTo(expected);
            assertThat(result.currency()).isEqualTo(BRL);
        }

        @Test
        @DisplayName("Should verify whether it is exact (no floating point error)")
        void shouldVerifyWhetherItIsExact() {
            Money result = brl("0.1").sum(brl("0.2"));
            assertThat(result.amount()).isEqualByComparingTo("0.3");
        }

        @Test
        @DisplayName("Should keep the larger scale of the operands")
        void shouldKeepLargerScale() {
            Money result = brl("1.10").sum(brl("2.205"));
            assertThat(result.amount()).isEqualTo(new BigDecimal("3.305"));
            assertThat(result.amount().scale()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return a new instance and not mutate operands")
        void shouldReturnANewInstanceAndNotMutateOperands() {
            Money a = brl("10.00");
            Money b = brl("5.00");
            Money result = a.sum(b);
            assertThat(result).isNotSameAs(a).isNotSameAs(b);
            assertThat(a.amount()).isEqualTo(new BigDecimal("10.00"));
            assertThat(b.amount()).isEqualTo(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("Should be commutative")
        void shouldBeCommutative() {
            Money a = brl("12.34");
            Money b = brl("56.78");
            assertThat(a.sum(b)).isEqualTo(b.sum(a));
        }

        @Test
        @DisplayName("Should be associative")
        void shouldBeAssociative() {
            Money a = brl("1.11");
            Money b = brl("2.22");
            Money c = brl("3.33");
            assertThat(a.sum(b).sum(c)).isEqualTo(a.sum(b.sum(c)));
        }


        @Test
        @DisplayName("Should have zero as the identity element")
        void shouldHaveZeroAsIdentity() {
            Money a = brl("42.00");
            Money zero = brl("0.00");
            assertThat(a.sum(zero)).isEqualTo(a);
            assertThat(zero.sum(a)).isEqualTo(a);
        }

        @Test
        @DisplayName("Should be chained")
        void shouldBeChainable() {
            Money result = brl("1.00").sum(brl("2.00")).sum(brl("3.00"));
            assertThat(result.amount()).isEqualByComparingTo("6.00");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should reject null operand with NullPointerException")
        void shouldRejectNullOperand(Money other) {
            assertThatNullPointerException()
                    .isThrownBy(() -> brl("10.00").sum(other))
                    .withMessage("Amount cannot be null");
        }

    }

    @Nested
    @DisplayName("equality, hashCode and toString")
    class ValueObjectContract {
        @Test
        @DisplayName("Should be equal when amount (same scale) and currency match")
        void shouldBeEqualWhenSameValues() {
            Money a = brl("10.00");
            Money b = brl("10.00");
            assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        }

        @Test
        @DisplayName("Should not be equal when the amount differ")
        void shouldNotBeEqualWhenTheAmountsDiffer() {
            assertThat(brl("10.00")).isNotEqualTo(brl("10.01"));
        }

        @Test
        @DisplayName("Should not be equal when amount has same value but different scale")
        void shouldVerifyThatBeSameNumericAmountWithDifferentScales() {
            Money a = brl("10.0");
            Money b = brl("10.00");
            assertThat(a.amount()).isEqualByComparingTo(b.amount());
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("Should not be equal to null or another type")
        void shouldNotBeEqualToNullOrOtherType() {
            Money a = brl("10.00");
            assertThat(a).isNotEqualTo(null);
            assertThat(a).isNotEqualTo("10.00 BRL");
        }

        @Test
        @DisplayName("Should expose components through accessors")
        void shouldExposeComponentsThroughAccessors() {
            Money a = brl("10.50");
            assertThat(a.amount()).isEqualTo(new BigDecimal("10.50"));
            assertThat(a.currency()).isEqualTo("BRL");
        }

        @Test
        @DisplayName("Should contain the amount and currency and be readable")
        void shouldContainAmountAndCurrencyAndBeReadable() {
            assertThat(brl("10.50").toString()).contains("10.50").contains("BRL");
        }

    }
}
