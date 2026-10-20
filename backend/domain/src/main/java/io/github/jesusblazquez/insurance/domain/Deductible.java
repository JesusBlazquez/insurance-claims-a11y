package io.github.jesusblazquez.insurance.domain;

import java.math.BigDecimal;

/**
 * The excess the policy holder pays out of their own pocket on every claim.
 *
 * <p>Modelled as an enum rather than a free amount on purpose: the business sells three excess
 * levels, so any other figure is a bug. It also means the pricing discount cannot drift away from
 * the excess it belongs to, because both live here.
 */
public enum Deductible {
    NONE(Money.euros("0.00"), new BigDecimal("0.00")),
    MEDIUM(Money.euros("300.00"), new BigDecimal("0.05")),
    HIGH(Money.euros("600.00"), new BigDecimal("0.10"));

    private final Money amount;
    private final BigDecimal discount;

    Deductible(Money amount, BigDecimal discount) {
        this.amount = amount;
        this.discount = discount;
    }

    public Money amount() {
        return amount;
    }

    /** The share taken off the premium for accepting this excess, as a factor between 0 and 1. */
    public BigDecimal discount() {
        return discount;
    }
}
