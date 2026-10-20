package io.github.jesusblazquez.insurance.domain;

import io.github.jesusblazquez.insurance.domain.exception.IllegalPolicyTransitionException;
import io.github.jesusblazquez.insurance.domain.exception.InvalidAmountException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A car insurance contract.
 *
 * <p>The method worth reading is {@link #wasInForceOn(LocalDate)}. Everything else is bookkeeping.
 */
public final class Policy {

    private final UUID id;
    private final PolicyNumber policyNumber;
    private final UUID customerId;
    private final Vehicle vehicle;
    private final Coverage coverage;
    private final Money insuredSum;
    private final Deductible deductible;
    private final LocalDate validFrom;
    private final LocalDate validTo;
    private final Money premium;
    private PolicyState state;

    private Policy(
            UUID id,
            PolicyNumber policyNumber,
            UUID customerId,
            Vehicle vehicle,
            Coverage coverage,
            Money insuredSum,
            Deductible deductible,
            LocalDate validFrom,
            LocalDate validTo,
            Money premium,
            PolicyState state) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.policyNumber = Objects.requireNonNull(policyNumber, "policy number is required");
        this.customerId = Objects.requireNonNull(customerId, "customer is required");
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle is required");
        this.coverage = Objects.requireNonNull(coverage, "coverage is required");
        this.insuredSum = Objects.requireNonNull(insuredSum, "insured sum is required");
        this.deductible = Objects.requireNonNull(deductible, "deductible is required");
        this.validFrom = Objects.requireNonNull(validFrom, "start date is required");
        this.validTo = Objects.requireNonNull(validTo, "end date is required");
        this.premium = Objects.requireNonNull(premium, "premium is required");
        this.state = Objects.requireNonNull(state, "state is required");
        if (!insuredSum.isPositive()) {
            throw new InvalidAmountException("insured sum");
        }
        if (!premium.isPositive()) {
            throw new InvalidAmountException("premium");
        }
        if (!validTo.isAfter(validFrom)) {
            throw new IllegalArgumentException("The end date must be later than the start date");
        }
    }

    /** Creates a policy that has been quoted but not yet issued. */
    public static Policy draft(
            UUID id,
            PolicyNumber policyNumber,
            UUID customerId,
            Vehicle vehicle,
            Coverage coverage,
            Money insuredSum,
            Deductible deductible,
            LocalDate validFrom,
            LocalDate validTo,
            Money premium) {
        return new Policy(
                id,
                policyNumber,
                customerId,
                vehicle,
                coverage,
                insuredSum,
                deductible,
                validFrom,
                validTo,
                premium,
                new PolicyState.Draft());
    }

    /** Rebuilds a policy from storage without replaying its history. */
    public static Policy rehydrate(
            UUID id,
            PolicyNumber policyNumber,
            UUID customerId,
            Vehicle vehicle,
            Coverage coverage,
            Money insuredSum,
            Deductible deductible,
            LocalDate validFrom,
            LocalDate validTo,
            Money premium,
            PolicyState state) {
        return new Policy(
                id,
                policyNumber,
                customerId,
                vehicle,
                coverage,
                insuredSum,
                deductible,
                validFrom,
                validTo,
                premium,
                state);
    }

    public void issue(LocalDate issuedOn) {
        Objects.requireNonNull(issuedOn, "issue date is required");
        if (!(state instanceof PolicyState.Draft)) {
            throw new IllegalPolicyTransitionException(state.label(), "issued");
        }
        state = new PolicyState.Active(issuedOn);
    }

    public void cancel(LocalDate cancelledOn, String reason) {
        Objects.requireNonNull(cancelledOn, "cancellation date is required");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A cancellation reason is required");
        }
        if (!(state instanceof PolicyState.Active)) {
            throw new IllegalPolicyTransitionException(state.label(), "cancelled");
        }
        state = new PolicyState.Cancelled(cancelledOn, reason.trim());
    }

    /** Marks an active policy as expired, but only once its cover period is actually over. */
    public void expire(LocalDate today) {
        Objects.requireNonNull(today, "today is required");
        if (!(state instanceof PolicyState.Active)) {
            throw new IllegalPolicyTransitionException(state.label(), "expired");
        }
        if (!today.isAfter(validTo)) {
            throw new IllegalPolicyTransitionException(state.label(), "expired before its end date");
        }
        state = new PolicyState.Expired(today);
    }

    /**
     * Whether this policy covered the vehicle on a given day.
     *
     * <p>This is the rule the whole project hangs on. What matters is the day of the incident, not
     * today: a policy that expired last month still covers a crash that happened while it was
     * running. A cancelled policy, on the other hand, stops covering from the day it was cancelled,
     * not from the day it was sold.
     */
    public boolean wasInForceOn(LocalDate date) {
        Objects.requireNonNull(date, "date is required");
        if (date.isBefore(validFrom) || date.isAfter(validTo)) {
            return false;
        }
        return switch (state) {
            case PolicyState.Draft ignored -> false;
            case PolicyState.Active ignored -> true;
            case PolicyState.Expired ignored -> true;
            case PolicyState.Cancelled cancelled -> date.isBefore(cancelled.cancelledOn());
        };
    }

    public UUID id() {
        return id;
    }

    public PolicyNumber policyNumber() {
        return policyNumber;
    }

    public UUID customerId() {
        return customerId;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public Coverage coverage() {
        return coverage;
    }

    public Money insuredSum() {
        return insuredSum;
    }

    public Deductible deductible() {
        return deductible;
    }

    public LocalDate validFrom() {
        return validFrom;
    }

    public LocalDate validTo() {
        return validTo;
    }

    public Money premium() {
        return premium;
    }

    public PolicyState state() {
        return state;
    }
}
