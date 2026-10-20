package io.github.jesusblazquez.insurance.domain;

import io.github.jesusblazquez.insurance.domain.exception.CurrencyMismatchException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * An amount of money in a given currency.
 *
 * <p>Money is never a {@code double}: 0.10 has no exact binary representation and the error grows
 * with every operation. It is never a bare {@code BigDecimal} either, because then nothing stops
 * euros from being added to dollars.
 *
 * <p>Amounts are normalised to two decimals in the constructor, which also makes equality behave as
 * expected: {@code 100.00} equals {@code 100.0}, which would not hold for raw {@code BigDecimal}
 * values with different scales.
 */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public static final Currency EUR = Currency.getInstance("EUR");

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public Money {
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        amount = amount.setScale(SCALE, ROUNDING);
    }

    public static Money euros(String amount) {
        return new Money(new BigDecimal(amount), EUR);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    /** Multiplies by a plain factor, used by the pricing rules. */
    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor is required");
        return new Money(amount.multiply(factor), currency);
    }

    /** Returns this amount, or zero when it is negative. Used when a deductible eats a payout. */
    public Money orZeroIfNegative() {
        return isNegative() ? zero(currency) : this;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(amount.toPlainString(), currency.getCurrencyCode());
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "the other amount is required");
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
    }
}
