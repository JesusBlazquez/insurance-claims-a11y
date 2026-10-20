package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when a plate number does not follow the Spanish format of four digits and three letters. */
public class InvalidPlateNumberException extends DomainException {

    public InvalidPlateNumberException(String value) {
        super("'%s' is not a valid plate number".formatted(value));
    }
}
