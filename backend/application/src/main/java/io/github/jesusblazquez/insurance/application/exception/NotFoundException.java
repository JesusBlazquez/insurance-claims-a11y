package io.github.jesusblazquez.insurance.application.exception;

/**
 * Something the caller asked for by identifier does not exist.
 *
 * <p>This is an application concern, not a domain one: the domain never looks anything up, it is
 * handed the objects it needs. Adapters map this single type to a 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String what, Object id) {
        super("%s %s was not found".formatted(what, id));
    }
}
