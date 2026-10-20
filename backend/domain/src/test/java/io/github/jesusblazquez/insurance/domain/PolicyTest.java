package io.github.jesusblazquez.insurance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.domain.exception.IllegalPolicyTransitionException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PolicyTest {

    private static final LocalDate VALID_FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate VALID_TO = LocalDate.of(2026, 12, 31);

    @Nested
    @DisplayName("state transitions")
    class Transitions {

        @Test
        @DisplayName("a draft policy becomes active when issued")
        void issuesADraft() {
            Policy policy = draftPolicy();

            policy.issue(VALID_FROM);

            assertThat(policy.state()).isInstanceOf(PolicyState.Active.class);
            assertThat(policy.state().label()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("a policy cannot be issued twice")
        void refusesToIssueTwice() {
            Policy policy = draftPolicy();
            policy.issue(VALID_FROM);

            assertThatThrownBy(() -> policy.issue(VALID_FROM))
                    .isInstanceOf(IllegalPolicyTransitionException.class)
                    .hasMessageContaining("ACTIVE");
        }

        @Test
        @DisplayName("a draft policy cannot be cancelled, because it was never sold")
        void refusesToCancelADraft() {
            Policy policy = draftPolicy();

            assertThatThrownBy(() -> policy.cancel(VALID_FROM, "customer changed their mind"))
                    .isInstanceOf(IllegalPolicyTransitionException.class);
        }

        @Test
        @DisplayName("cancelling requires a reason")
        void refusesToCancelWithoutReason() {
            Policy policy = draftPolicy();
            policy.issue(VALID_FROM);

            assertThatThrownBy(() -> policy.cancel(VALID_FROM.plusMonths(2), "  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("a policy cannot expire before its cover period is over")
        void refusesToExpireEarly() {
            Policy policy = draftPolicy();
            policy.issue(VALID_FROM);

            assertThatThrownBy(() -> policy.expire(VALID_TO)).isInstanceOf(IllegalPolicyTransitionException.class);
        }

        @Test
        @DisplayName("a policy expires the day after its cover period ends")
        void expiresAfterTheCoverPeriod() {
            Policy policy = draftPolicy();
            policy.issue(VALID_FROM);

            policy.expire(VALID_TO.plusDays(1));

            assertThat(policy.state().label()).isEqualTo("EXPIRED");
        }
    }

    @Nested
    @DisplayName("cover on a given day")
    class CoverOnAGivenDay {

        @Test
        @DisplayName("a draft policy covers nothing: it was never issued")
        void draftCoversNothing() {
            assertThat(draftPolicy().wasInForceOn(LocalDate.of(2026, 6, 1))).isFalse();
        }

        @Test
        @DisplayName("an active policy covers any day inside its period")
        void activeCoversItsPeriod() {
            Policy policy = issuedPolicy();

            assertThat(policy.wasInForceOn(VALID_FROM)).isTrue();
            assertThat(policy.wasInForceOn(LocalDate.of(2026, 6, 1))).isTrue();
            assertThat(policy.wasInForceOn(VALID_TO)).isTrue();
        }

        @Test
        @DisplayName("no policy covers days outside its period")
        void coversNothingOutsideItsPeriod() {
            Policy policy = issuedPolicy();

            assertThat(policy.wasInForceOn(VALID_FROM.minusDays(1))).isFalse();
            assertThat(policy.wasInForceOn(VALID_TO.plusDays(1))).isFalse();
        }

        @Test
        @DisplayName("an expired policy still covers the days it was running")
        void expiredStillCoversThePast() {
            Policy policy = issuedPolicy();
            policy.expire(VALID_TO.plusDays(1));

            assertThat(policy.wasInForceOn(LocalDate.of(2026, 6, 1))).isTrue();
        }

        @Test
        @DisplayName("a cancelled policy covers up to the day before it was cancelled, and no further")
        void cancelledCoversUntilTheCancellationDate() {
            Policy policy = issuedPolicy();
            LocalDate cancelledOn = LocalDate.of(2026, 6, 1);
            policy.cancel(cancelledOn, "unpaid premium");

            assertThat(policy.wasInForceOn(cancelledOn.minusDays(1))).isTrue();
            assertThat(policy.wasInForceOn(cancelledOn)).isFalse();
            assertThat(policy.wasInForceOn(cancelledOn.plusDays(1))).isFalse();
        }
    }

    @Test
    @DisplayName("the end date must be later than the start date")
    void refusesAnInvalidPeriod() {
        assertThatThrownBy(() -> Policy.draft(
                        UUID.randomUUID(),
                        PolicyNumber.of("POL-2026-000001"),
                        UUID.randomUUID(),
                        vehicle(),
                        Coverage.COMPREHENSIVE,
                        Money.euros("20000.00"),
                        Deductible.MEDIUM,
                        VALID_TO,
                        VALID_FROM,
                        Money.euros("513.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static Policy draftPolicy() {
        return Policy.draft(
                UUID.randomUUID(),
                PolicyNumber.of("POL-2026-000001"),
                UUID.randomUUID(),
                vehicle(),
                Coverage.COMPREHENSIVE,
                Money.euros("20000.00"),
                Deductible.MEDIUM,
                VALID_FROM,
                VALID_TO,
                Money.euros("513.00"));
    }

    static Policy issuedPolicy() {
        Policy policy = draftPolicy();
        policy.issue(VALID_FROM);
        return policy;
    }

    static Vehicle vehicle() {
        return new Vehicle(PlateNumber.of("1234BCD"), "Seat", "Leon", 110, 2020);
    }
}
