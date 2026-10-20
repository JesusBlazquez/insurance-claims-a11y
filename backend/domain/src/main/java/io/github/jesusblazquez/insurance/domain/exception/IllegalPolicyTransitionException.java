package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when a policy is asked to move to a state it cannot reach from the current one. */
public class IllegalPolicyTransitionException extends DomainException {

    public IllegalPolicyTransitionException(String from, String action) {
        super("A policy in state %s cannot be %s".formatted(from, action));
    }
}
