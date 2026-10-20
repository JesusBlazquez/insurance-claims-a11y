package io.github.jesusblazquez.insurance.domain;

import java.time.LocalDate;

/**
 * The state of a policy, modelled as a sealed hierarchy instead of a plain enum.
 *
 * <p>Each state carries the data that only makes sense in that state: a cancelled policy has a
 * cancellation date and a reason, a draft has neither. With an enum that data would have to live on
 * the policy as nullable fields, and nothing would stop code from reading a cancellation reason
 * from an active policy.
 */
public sealed interface PolicyState {

    record Draft() implements PolicyState {}

    record Active(LocalDate issuedOn) implements PolicyState {}

    record Cancelled(LocalDate cancelledOn, String reason) implements PolicyState {}

    record Expired(LocalDate expiredOn) implements PolicyState {}

    /** Stable name for persistence and for the API, independent of the Java class name. */
    default String label() {
        return switch (this) {
            case Draft ignored -> "DRAFT";
            case Active ignored -> "ACTIVE";
            case Cancelled ignored -> "CANCELLED";
            case Expired ignored -> "EXPIRED";
        };
    }
}
