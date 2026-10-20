package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when an amount is zero or negative where the business requires a positive figure. */
public class InvalidAmountException extends DomainException {

    public InvalidAmountException(String what) {
        super("The %s must be greater than zero".formatted(what));
    }
}
