package io.github.jesusblazquez.insurance.application.exception;

/**
 * A customer with that NIF is already registered.
 *
 * <p>The uniqueness of a NIF cannot be checked inside the aggregate, because an aggregate only
 * knows about itself. It is checked here and, as a last line of defence, by a unique index in the
 * database: two simultaneous registrations would both pass this check.
 */
public class DuplicateNifException extends RuntimeException {

    public DuplicateNifException(String nif) {
        super("A customer with NIF %s is already registered".formatted(nif));
    }
}
