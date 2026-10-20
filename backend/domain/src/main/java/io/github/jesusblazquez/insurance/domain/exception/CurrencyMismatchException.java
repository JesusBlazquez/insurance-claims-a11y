package io.github.jesusblazquez.insurance.domain.exception;

import java.util.Currency;

/** Thrown when two amounts in different currencies are combined. */
public class CurrencyMismatchException extends DomainException {

    public CurrencyMismatchException(Currency expected, Currency actual) {
        super("Cannot operate on %s and %s".formatted(expected.getCurrencyCode(), actual.getCurrencyCode()));
    }
}
