package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when a claim is asked to move to a state it cannot reach from the current one. */
public class IllegalClaimTransitionException extends DomainException {

    public IllegalClaimTransitionException(String from, String action) {
        super("A claim in state %s cannot be %s".formatted(from, action));
    }
}
