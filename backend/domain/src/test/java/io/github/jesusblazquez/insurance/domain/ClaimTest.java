package io.github.jesusblazquez.insurance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.domain.exception.ClaimAmountExceedsInsuredSumException;
import io.github.jesusblazquez.insurance.domain.exception.IllegalClaimTransitionException;
import io.github.jesusblazquez.insurance.domain.exception.InvalidOccurrenceDateException;
import io.github.jesusblazquez.insurance.domain.exception.PolicyNotInForceException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClaimTest {

    private static final LocalDate CRASH_DAY = LocalDate.of(2026, 6, 1);
    private static final LocalDate REPORTED_ON = LocalDate.of(2026, 6, 3);
    private static final ClaimNumber NUMBER = ClaimNumber.of("CLM-2026-000001");

    @Nested
    @DisplayName("opening a claim")
    class Opening {

        @Test
        @DisplayName("opens against a policy that covered the vehicle that day")
        void opensAgainstAPolicyInForce() {
            Claim claim = openClaim(PolicyTest.issuedPolicy(), CRASH_DAY, REPORTED_ON, "2000.00");

            assertThat(claim.state().label()).isEqualTo("OPEN");
            assertThat(claim.claimedAmount()).isEqualTo(Money.euros("2000.00"));
        }

        @Test
        @DisplayName("a policy that has since expired still covers an incident from when it was running")
        void acceptsAnIncidentCoveredByANowExpiredPolicy() {
            Policy policy = PolicyTest.issuedPolicy();
            policy.expire(LocalDate.of(2027, 1, 1));

            Claim claim = openClaim(policy, CRASH_DAY, LocalDate.of(2027, 1, 5), "2000.00");

            assertThat(claim.occurredOn()).isEqualTo(CRASH_DAY);
        }

        @Test
        @DisplayName("refuses an incident from after the policy was cancelled")
        void refusesAnIncidentAfterCancellation() {
            Policy policy = PolicyTest.issuedPolicy();
            policy.cancel(CRASH_DAY.minusDays(1), "unpaid premium");

            assertThatThrownBy(() -> openClaim(policy, CRASH_DAY, REPORTED_ON, "2000.00"))
                    .isInstanceOf(PolicyNotInForceException.class)
                    .hasMessageContaining("2026-06-01");
        }

        @Test
        @DisplayName("refuses an incident against a policy that was never issued")
        void refusesAnIncidentAgainstADraft() {
            assertThatThrownBy(() -> openClaim(PolicyTest.draftPolicy(), CRASH_DAY, REPORTED_ON, "2000.00"))
                    .isInstanceOf(PolicyNotInForceException.class);
        }

        @Test
        @DisplayName("refuses an incident dated after the day it was reported")
        void refusesAFutureIncident() {
            assertThatThrownBy(
                            () -> openClaim(PolicyTest.issuedPolicy(), REPORTED_ON.plusDays(1), REPORTED_ON, "2000.00"))
                    .isInstanceOf(InvalidOccurrenceDateException.class);
        }

        @Test
        @DisplayName("refuses a claim for more than the sum insured")
        void refusesToClaimMoreThanInsured() {
            assertThatThrownBy(() -> openClaim(PolicyTest.issuedPolicy(), CRASH_DAY, REPORTED_ON, "20000.01"))
                    .isInstanceOf(ClaimAmountExceedsInsuredSumException.class);
        }

        @Test
        @DisplayName("copies the excess in force when the claim is filed, so later changes cannot move it")
        void snapshotsTheExcess() {
            Claim claim = openClaim(PolicyTest.issuedPolicy(), CRASH_DAY, REPORTED_ON, "2000.00");

            assertThat(claim.deductibleAtOpening()).isEqualTo(Money.euros("300.00"));
            assertThat(claim.insuredSumAtOpening()).isEqualTo(Money.euros("20000.00"));
        }
    }

    @Nested
    @DisplayName("settling a claim")
    class Settling {

        @Test
        @DisplayName("runs the full path from open to paid")
        void runsTheHappyPath() {
            Claim claim = openClaim(PolicyTest.issuedPolicy(), CRASH_DAY, REPORTED_ON, "2000.00");

            claim.startReview(REPORTED_ON);
            claim.approve(REPORTED_ON.plusDays(5));
            claim.pay(REPORTED_ON.plusDays(10));

            assertThat(claim.state()).isInstanceOf(ClaimState.Paid.class);
            ClaimState.Paid paid = (ClaimState.Paid) claim.state();
            assertThat(paid.paidAmount()).isEqualTo(Money.euros("1700.00"));
        }

        @Test
        @DisplayName("takes the excess off the amount approved")
        void subtractsTheExcess() {
            Claim claim = reviewedClaim("2000.00");

            claim.approve(REPORTED_ON.plusDays(5));

            ClaimState.Approved approved = (ClaimState.Approved) claim.state();
            assertThat(approved.approvedAmount()).isEqualTo(Money.euros("1700.00"));
        }

        @Test
        @DisplayName("pays nothing when the excess swallows the claim, and never a negative amount")
        void floorsThePayoutAtZero() {
            Claim claim = reviewedClaim("250.00");

            claim.approve(REPORTED_ON.plusDays(5));

            ClaimState.Approved approved = (ClaimState.Approved) claim.state();
            assertThat(approved.approvedAmount()).isEqualTo(Money.euros("0.00"));
        }

        @Test
        @DisplayName("cannot be approved before it has been reviewed")
        void refusesToApproveBeforeReview() {
            Claim claim = openClaim(PolicyTest.issuedPolicy(), CRASH_DAY, REPORTED_ON, "2000.00");

            assertThatThrownBy(() -> claim.approve(REPORTED_ON))
                    .isInstanceOf(IllegalClaimTransitionException.class)
                    .hasMessageContaining("OPEN");
        }

        @Test
        @DisplayName("cannot be paid before it has been approved")
        void refusesToPayBeforeApproval() {
            Claim claim = reviewedClaim("2000.00");

            assertThatThrownBy(() -> claim.pay(REPORTED_ON))
                    .isInstanceOf(IllegalClaimTransitionException.class)
                    .hasMessageContaining("UNDER_REVIEW");
        }

        @Test
        @DisplayName("cannot be rejected once it has been approved")
        void refusesToRejectAnApprovedClaim() {
            Claim claim = reviewedClaim("2000.00");
            claim.approve(REPORTED_ON.plusDays(1));

            assertThatThrownBy(() -> claim.reject(REPORTED_ON.plusDays(2), "fraud suspected"))
                    .isInstanceOf(IllegalClaimTransitionException.class);
        }

        @Test
        @DisplayName("rejecting requires a reason the customer can be told")
        void refusesToRejectWithoutReason() {
            Claim claim = reviewedClaim("2000.00");

            assertThatThrownBy(() -> claim.reject(REPORTED_ON.plusDays(1), ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("records the reason when rejected")
        void recordsTheRejectionReason() {
            Claim claim = reviewedClaim("2000.00");

            claim.reject(REPORTED_ON.plusDays(1), "damage predates the cover");

            ClaimState.Rejected rejected = (ClaimState.Rejected) claim.state();
            assertThat(rejected.reason()).isEqualTo("damage predates the cover");
        }
    }

    private static Claim reviewedClaim(String amount) {
        Claim claim = openClaim(PolicyTest.issuedPolicy(), CRASH_DAY, REPORTED_ON, amount);
        claim.startReview(REPORTED_ON);
        return claim;
    }

    private static Claim openClaim(Policy policy, LocalDate occurredOn, LocalDate reportedOn, String amount) {
        return Claim.open(
                UUID.randomUUID(),
                NUMBER,
                policy,
                occurredOn,
                reportedOn,
                "Rear-ended at a roundabout, bumper and tailgate damaged",
                Money.euros(amount));
    }
}
