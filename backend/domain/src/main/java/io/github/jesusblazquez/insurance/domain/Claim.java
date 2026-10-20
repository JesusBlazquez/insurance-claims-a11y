package io.github.jesusblazquez.insurance.domain;

import io.github.jesusblazquez.insurance.domain.exception.ClaimAmountExceedsInsuredSumException;
import io.github.jesusblazquez.insurance.domain.exception.IllegalClaimTransitionException;
import io.github.jesusblazquez.insurance.domain.exception.InvalidAmountException;
import io.github.jesusblazquez.insurance.domain.exception.InvalidOccurrenceDateException;
import io.github.jesusblazquez.insurance.domain.exception.PolicyNotInForceException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A claim filed against a policy.
 *
 * <p>The insured sum and the excess are copied in when the claim is opened instead of being read
 * from the policy later. A claim is settled under the terms that applied on the day it was filed,
 * so if the policy is renewed with a different excess next year, this claim must not change.
 */
public final class Claim {

    private static final int MIN_DESCRIPTION = 10;
    private static final int MAX_DESCRIPTION = 1000;

    private final UUID id;
    private final ClaimNumber claimNumber;
    private final UUID policyId;
    private final LocalDate occurredOn;
    private final LocalDate reportedOn;
    private final String description;
    private final Money claimedAmount;
    private final Money insuredSumAtOpening;
    private final Money deductibleAtOpening;
    private ClaimState state;

    private Claim(
            UUID id,
            ClaimNumber claimNumber,
            UUID policyId,
            LocalDate occurredOn,
            LocalDate reportedOn,
            String description,
            Money claimedAmount,
            Money insuredSumAtOpening,
            Money deductibleAtOpening,
            ClaimState state) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.claimNumber = Objects.requireNonNull(claimNumber, "claim number is required");
        this.policyId = Objects.requireNonNull(policyId, "policy is required");
        this.occurredOn = Objects.requireNonNull(occurredOn, "occurrence date is required");
        this.reportedOn = Objects.requireNonNull(reportedOn, "report date is required");
        this.claimedAmount = Objects.requireNonNull(claimedAmount, "claimed amount is required");
        this.insuredSumAtOpening = Objects.requireNonNull(insuredSumAtOpening, "insured sum is required");
        this.deductibleAtOpening = Objects.requireNonNull(deductibleAtOpening, "deductible is required");
        this.state = Objects.requireNonNull(state, "state is required");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A description is required");
        }
        this.description = description.trim();
        if (this.description.length() < MIN_DESCRIPTION || this.description.length() > MAX_DESCRIPTION) {
            throw new IllegalArgumentException(
                    "The description must be between %d and %d characters".formatted(MIN_DESCRIPTION, MAX_DESCRIPTION));
        }
    }

    /**
     * Opens a claim against a policy.
     *
     * <p>The report date is the day the claim reaches the insurer, normally today. Requiring the
     * incident not to be later than the report date is what rules out incidents in the future,
     * without the domain ever reading a clock.
     */
    public static Claim open(
            UUID id,
            ClaimNumber claimNumber,
            Policy policy,
            LocalDate occurredOn,
            LocalDate reportedOn,
            String description,
            Money claimedAmount) {
        Objects.requireNonNull(policy, "policy is required");
        Objects.requireNonNull(occurredOn, "occurrence date is required");
        Objects.requireNonNull(reportedOn, "report date is required");
        Objects.requireNonNull(claimedAmount, "claimed amount is required");

        if (occurredOn.isAfter(reportedOn)) {
            throw new InvalidOccurrenceDateException(
                    "The incident date %s cannot be later than the date it was reported, %s"
                            .formatted(occurredOn, reportedOn));
        }
        if (!policy.wasInForceOn(occurredOn)) {
            throw new PolicyNotInForceException(policy.policyNumber().value(), occurredOn);
        }
        if (!claimedAmount.isPositive()) {
            throw new InvalidAmountException("claimed amount");
        }
        if (claimedAmount.isGreaterThan(policy.insuredSum())) {
            throw new ClaimAmountExceedsInsuredSumException(
                    claimedAmount.toString(), policy.insuredSum().toString());
        }
        return new Claim(
                id,
                claimNumber,
                policy.id(),
                occurredOn,
                reportedOn,
                description,
                claimedAmount,
                policy.insuredSum(),
                policy.deductible().amount(),
                new ClaimState.Open());
    }

    /** Rebuilds a claim from storage. */
    public static Claim rehydrate(
            UUID id,
            ClaimNumber claimNumber,
            UUID policyId,
            LocalDate occurredOn,
            LocalDate reportedOn,
            String description,
            Money claimedAmount,
            Money insuredSumAtOpening,
            Money deductibleAtOpening,
            ClaimState state) {
        return new Claim(
                id,
                claimNumber,
                policyId,
                occurredOn,
                reportedOn,
                description,
                claimedAmount,
                insuredSumAtOpening,
                deductibleAtOpening,
                state);
    }

    public void startReview(LocalDate startedOn) {
        Objects.requireNonNull(startedOn, "start date is required");
        if (!(state instanceof ClaimState.Open)) {
            throw new IllegalClaimTransitionException(state.label(), "sent to review");
        }
        state = new ClaimState.UnderReview(startedOn);
    }

    /**
     * Approves the claim and works out what is actually payable: the amount claimed less the
     * excess, never below zero.
     */
    public void approve(LocalDate approvedOn) {
        Objects.requireNonNull(approvedOn, "approval date is required");
        if (!(state instanceof ClaimState.UnderReview)) {
            throw new IllegalClaimTransitionException(state.label(), "approved");
        }
        Money payable = claimedAmount.subtract(deductibleAtOpening).orZeroIfNegative();
        state = new ClaimState.Approved(payable, approvedOn);
    }

    public void reject(LocalDate rejectedOn, String reason) {
        Objects.requireNonNull(rejectedOn, "rejection date is required");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        if (!(state instanceof ClaimState.UnderReview)) {
            throw new IllegalClaimTransitionException(state.label(), "rejected");
        }
        state = new ClaimState.Rejected(reason.trim(), rejectedOn);
    }

    public void pay(LocalDate paidOn) {
        Objects.requireNonNull(paidOn, "payment date is required");
        if (!(state instanceof ClaimState.Approved approved)) {
            throw new IllegalClaimTransitionException(state.label(), "paid");
        }
        state = new ClaimState.Paid(approved.approvedAmount(), paidOn);
    }

    public UUID id() {
        return id;
    }

    public ClaimNumber claimNumber() {
        return claimNumber;
    }

    public UUID policyId() {
        return policyId;
    }

    public LocalDate occurredOn() {
        return occurredOn;
    }

    public LocalDate reportedOn() {
        return reportedOn;
    }

    public String description() {
        return description;
    }

    public Money claimedAmount() {
        return claimedAmount;
    }

    public Money insuredSumAtOpening() {
        return insuredSumAtOpening;
    }

    public Money deductibleAtOpening() {
        return deductibleAtOpening;
    }

    public ClaimState state() {
        return state;
    }
}
