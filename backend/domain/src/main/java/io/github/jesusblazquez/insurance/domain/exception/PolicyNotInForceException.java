package io.github.jesusblazquez.insurance.domain.exception;

import java.time.LocalDate;

/**
 * Thrown when a claim is opened for a date on which the policy did not cover the vehicle.
 *
 * <p>The date that matters is the day of the incident, not today.
 */
public class PolicyNotInForceException extends DomainException {

    public PolicyNotInForceException(String policyNumber, LocalDate occurredOn) {
        super("Policy %s was not in force on %s".formatted(policyNumber, occurredOn));
    }
}
