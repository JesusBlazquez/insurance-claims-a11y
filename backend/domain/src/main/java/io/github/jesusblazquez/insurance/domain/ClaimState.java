package io.github.jesusblazquez.insurance.domain;

import java.time.LocalDate;

/**
 * The state of a claim, with the data that belongs to each stage.
 *
 * <p>An approved claim has an amount, a rejected one has a reason, and neither exists while the
 * claim is merely open. Modelling it this way means the compiler, not a code review, is what
 * guarantees nobody reads an approved amount from a rejected claim.
 */
public sealed interface ClaimState {

    record Open() implements ClaimState {}

    record UnderReview(LocalDate startedOn) implements ClaimState {}

    record Approved(Money approvedAmount, LocalDate approvedOn) implements ClaimState {}

    record Rejected(String reason, LocalDate rejectedOn) implements ClaimState {}

    record Paid(Money paidAmount, LocalDate paidOn) implements ClaimState {}

    /** Stable name for persistence and for the API. */
    default String label() {
        return switch (this) {
            case Open ignored -> "OPEN";
            case UnderReview ignored -> "UNDER_REVIEW";
            case Approved ignored -> "APPROVED";
            case Rejected ignored -> "REJECTED";
            case Paid ignored -> "PAID";
        };
    }
}
