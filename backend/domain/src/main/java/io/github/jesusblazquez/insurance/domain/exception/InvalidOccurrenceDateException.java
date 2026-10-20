package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when the incident date is in the future or is later than the date it was reported. */
public class InvalidOccurrenceDateException extends DomainException {

    public InvalidOccurrenceDateException(String message) {
        super(message);
    }
}
