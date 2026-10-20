package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when someone under eighteen is registered as a policy holder. */
public class UnderageCustomerException extends DomainException {

    public UnderageCustomerException(int age) {
        super("A policy holder must be at least 18 years old, but this one is %d".formatted(age));
    }
}
