package io.github.jesusblazquez.insurance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.domain.exception.CurrencyMismatchException;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("normalises the scale so that 100.0 and 100.00 are the same amount")
    void normalisesScale() {
        assertThat(Money.euros("100.0")).isEqualTo(Money.euros("100.00"));
    }

    @Test
    @DisplayName("rounds half up to two decimals")
    void roundsToCents() {
        assertThat(Money.euros("10.555").amount()).isEqualByComparingTo("10.56");
    }

    @Test
    @DisplayName("adds and subtracts within the same currency")
    void addsAndSubtracts() {
        assertThat(Money.euros("100.00").add(Money.euros("50.50"))).isEqualTo(Money.euros("150.50"));
        assertThat(Money.euros("100.00").subtract(Money.euros("150.00"))).isEqualTo(Money.euros("-50.00"));
    }

    @Test
    @DisplayName("refuses to mix currencies, which is the whole point of not using BigDecimal")
    void refusesToMixCurrencies() {
        Money dollars = Money.of(new BigDecimal("10.00"), Currency.getInstance("USD"));
        assertThatThrownBy(() -> Money.euros("10.00").add(dollars)).isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    @DisplayName("floors a negative amount at zero")
    void floorsNegativeAmounts() {
        assertThat(Money.euros("-25.00").orZeroIfNegative()).isEqualTo(Money.euros("0.00"));
        assertThat(Money.euros("25.00").orZeroIfNegative()).isEqualTo(Money.euros("25.00"));
    }

    @Test
    @DisplayName("multiplies by a pricing factor")
    void multipliesByFactor() {
        assertThat(Money.euros("300.00").multiply(new BigDecimal("1.80"))).isEqualTo(Money.euros("540.00"));
    }
}
