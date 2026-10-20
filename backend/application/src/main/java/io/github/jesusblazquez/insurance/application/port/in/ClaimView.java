package io.github.jesusblazquez.insurance.application.port.in;

import io.github.jesusblazquez.insurance.domain.Claim;
import io.github.jesusblazquez.insurance.domain.ClaimState;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * What the application gives back about a claim.
 *
 * <p>The resolution figures are flattened here rather than exposing the sealed state, because the
 * boundary of the application is not the place to force a client to pattern match.
 */
public record ClaimView(
        UUID id,
        String claimNumber,
        UUID policyId,
        LocalDate occurredOn,
        LocalDate reportedOn,
        String description,
        BigDecimal claimedAmount,
        BigDecimal deductible,
        String currency,
        String status,
        BigDecimal settledAmount,
        String rejectionReason,
        LocalDate resolvedOn) {

    public static ClaimView of(Claim claim) {
        BigDecimal settled = null;
        String reason = null;
        LocalDate resolvedOn = null;
        switch (claim.state()) {
            case ClaimState.Approved approved -> {
                settled = approved.approvedAmount().amount();
                resolvedOn = approved.approvedOn();
            }
            case ClaimState.Paid paid -> {
                settled = paid.paidAmount().amount();
                resolvedOn = paid.paidOn();
            }
            case ClaimState.Rejected rejected -> {
                reason = rejected.reason();
                resolvedOn = rejected.rejectedOn();
            }
            case ClaimState.Open ignored -> {}
            case ClaimState.UnderReview ignored -> {}
        }
        return new ClaimView(
                claim.id(),
                claim.claimNumber().value(),
                claim.policyId(),
                claim.occurredOn(),
                claim.reportedOn(),
                claim.description(),
                claim.claimedAmount().amount(),
                claim.deductibleAtOpening().amount(),
                claim.claimedAmount().currency().getCurrencyCode(),
                claim.state().label(),
                settled,
                reason,
                resolvedOn);
    }
}
