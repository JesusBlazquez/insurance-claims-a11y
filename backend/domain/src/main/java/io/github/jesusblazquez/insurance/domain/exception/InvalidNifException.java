package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when a NIF is malformed or its check letter does not match the number. */
public class InvalidNifException extends DomainException {

    public InvalidNifException(String value) {
        super("'%s' is not a valid NIF".formatted(value));
    }
}
