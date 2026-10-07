package com.transaction.transferservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

import static com.transaction.transferservice.domain.model.Entry.EntryType.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Money tests")
class MoneyTests {

    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

    private static Money brl(String amount) {
        return new Money(new BigDecimal(amount), "BRL");
    }


    private static Entry base() {
        return new Entry("account-1", DEBIT, brl("100.00"), "ref-1", NOW);
    }



    @Nested
    @DisplayName("construction and accessors")
    class Construction {

        @Test
        @DisplayName("Should store all components and expose them through accessors")
        void shouldStoreAllComponentsAndExposeThemThroughAccessors() {
            Money amount = brl("250.75");
            Entry Entry = new Entry("account-9", CREDIT, amount, "pix-123", NOW);
            assertThat(Entry.accountId()).isEqualTo("account-9");
            assertThat(Entry.type()).isEqualTo(CREDIT);
            assertThat(Entry.amount()).isEqualTo(amount);
            assertThat(Entry.reference()).isEqualTo("pix-123");
            assertThat(Entry.instant()).isEqualTo(NOW);
        }


        @Test
        @DisplayName("Should keep the same amount instance")
        void shouldKeepTheSameAmountInstance() {
            Money amount = brl("10.00");
            Entry Entry = new Entry("account-1", CREDIT, amount, "ref-1", NOW);
            assertThat(Entry.amount()).isSameAs(amount);
        }

        @ParameterizedTest(name = "accepts type {0}")
        @EnumSource(Entry.EntryType.class)
        @DisplayName("Should accept every type")
        void shouldAcceptEveryType(Entry.EntryType type) {
            Entry Entry = new Entry("account-1", type, brl("1.00"), "ref-1", NOW);
            assertThat(Entry.type()).isEqualTo(type);
        }


        @Test
        @DisplayName("Should accept zero as amount (delegated to Money)")
        void shouldAcceptZeroValue() {
            assertThatCode(() -> new Entry("account-1", CREDIT, brl("0.00"), "ref-1", NOW))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should not be creatable with invalid money")
        void shouldNotBeCreatableWithInvalidMoney() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new Entry("account-1", CREDIT, brl("-1.00"), "ref-1", NOW))
                    .withMessage("Amount cannot be negative");
        }

    }

    @Nested
    @DisplayName("Type enum")
    class TypeEnum {

        @Test
        @DisplayName("Should declare exactly DEBIT and CREDIT, in this order")
        void shouldDeclareExpectedConstants() {
            assertThat(values()).containsExactly(DEBIT, CREDIT);
        }

        @ParameterizedTest(name = "valueOf(\"{0}\") resolves the constant")
        @ValueSource(strings = {"DEBIT", "CREDIT"})
        @DisplayName("Should resolve by name")
        void shouldResolveByName(String name) {
            assertThat(valueOf(name).name()).isEqualTo(name);
        }

        @ParameterizedTest(name = "valueOf(\"{0}\") fails")
        @ValueSource(strings = {"debit", "Credit", "REVERSAL", "", " DEBIT"})
        @DisplayName("Should reject unknown names")
        void shouldRejectUnknownNames(String name) {
            assertThatIllegalArgumentException().isThrownBy(() -> valueOf(name));
        }
    }


    @Nested
    @DisplayName("equality, hashCode and toString")
    class ValueObjectContract {

        /**
         * One variation per component: each differs from base() in exactly one field.
         */
        static Stream<Arguments> EntriesDifferingInOneField() {
            return Stream.of(
                    Arguments.of("accountId", new Entry("account-2", DEBIT, brl("100.00"), "ref-1", NOW)),
                    Arguments.of("EntryType", new Entry("account-1", CREDIT, brl("100.00"), "ref-1", NOW)),
                    Arguments.of("amount", new Entry("account-1", DEBIT, brl("100.01"), "ref-1", NOW)),
                    Arguments.of("reference", new Entry("account-1", DEBIT, brl("100.00"), "ref-2", NOW)),
                    Arguments.of("instant", new Entry("account-1", DEBIT, brl("100.00"), "ref-1", NOW.plusSeconds(1)))
            );
        }

        @Test
        @DisplayName("Should be equal when the same components")
        void shouldBeEqualWhenTheSameComponents() {
            Entry a = base();
            Entry b = base();
            assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        }

        @Test
        @DisplayName("Should be reflexive")
        void shouldBeReflexive() {
            Entry a = base();
            assertThat(a).isEqualTo(a);
        }

        @Test
        @DisplayName("Should be symmetric")
        void shouldBeSymmetric() {
            Entry a = base();
            Entry b = base();
            assertThat(a.equals(b)).isEqualTo(b.equals(a));
        }

        @Test
        @DisplayName("Should be transitive")
        void shouldBeTransitive() {
            Entry a = base();
            Entry b = base();
            Entry c = base();
            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(c);
            assertThat(a).isEqualTo(c);
        }

        @ParameterizedTest(name = "Should not be equal when only one ({0}) component differs")
        @MethodSource("EntriesDifferingInOneField")
        void shouldNotBeEqualWhenOneComponentDiffers(String field, Entry different) {
            assertThat(base()).isNotEqualTo(different);
        }

        @Test
        @DisplayName("Should not be equal to null or other type")
        void shouldNotBeEqualToNullOrOtherType() {
            assertThat(base()).isNotEqualTo(null);
            assertThat(base()).isNotEqualTo("account-1");
        }

        @Test
        @DisplayName("Should not be equal when amounts have the same value but different scale (known pitfall)")
        void documentsMoneyScalePitfall() {
            Entry a = new Entry("account-1", DEBIT, brl("10.0"), "ref-1", NOW);
            Entry b = new Entry("account-1", DEBIT, brl("10.00"), "ref-1", NOW);
            assertThat(a).isNotEqualTo(b); // inherited from BigDecimal.equals() inside Money
        }

        @Test
        @DisplayName("Should treat same instant as equal")
        void shouldTreatSameInstantAsEqual() {
            Entry a = new Entry("account-1", DEBIT, brl("1.00"), "ref-1",
                    Instant.parse("2026-10-01T12:00:00Z"));
            Entry b = new Entry("account-1", DEBIT, brl("1.00"), "ref-1",
                    Instant.ofEpochSecond(NOW.getEpochSecond()));
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("Should have readable to string")
        void shouldHaveReadableToString() {
            String text = base().toString();
            assertThat(text)
                    .contains("account-1")
                    .contains("DEBIT")
                    .contains("100.00")
                    .contains("ref-1")
                    .contains("2026-10-01T12:00:00Z");
        }
    }
}
