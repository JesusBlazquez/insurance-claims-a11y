package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when an email address is malformed. */
public class InvalidEmailException extends DomainException {

    public InvalidEmailException(String value) {
        super("'%s' is not a valid email address".formatted(value));
    }
}
