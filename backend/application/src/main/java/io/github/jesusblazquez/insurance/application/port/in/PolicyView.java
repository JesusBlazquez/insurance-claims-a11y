package io.github.jesusblazquez.insurance.application.port.in;

import io.github.jesusblazquez.insurance.domain.Policy;
import io.github.jesusblazquez.insurance.domain.PolicyState;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** What the application gives back about a policy. */
public record PolicyView(
        UUID id,
        String policyNumber,
        UUID customerId,
        String plate,
        String make,
        String model,
        int horsepower,
        int modelYear,
        String coverage,
        BigDecimal insuredSum,
        BigDecimal deductible,
        BigDecimal premium,
        String currency,
        LocalDate validFrom,
        LocalDate validTo,
        String status,
        LocalDate cancelledOn,
        String cancellationReason) {

    public static PolicyView of(Policy policy) {
        LocalDate cancelledOn = null;
        String reason = null;
        if (policy.state() instanceof PolicyState.Cancelled cancelled) {
            cancelledOn = cancelled.cancelledOn();
            reason = cancelled.reason();
        }
        return new PolicyView(
                policy.id(),
                policy.policyNumber().value(),
                policy.customerId(),
                policy.vehicle().plate().value(),
                policy.vehicle().make(),
                policy.vehicle().model(),
                policy.vehicle().horsepower(),
                policy.vehicle().modelYear(),
                policy.coverage().name(),
                policy.insuredSum().amount(),
                policy.deductible().amount().amount(),
                policy.premium().amount(),
                policy.premium().currency().getCurrencyCode(),
                policy.validFrom(),
                policy.validTo(),
                policy.state().label(),
                cancelledOn,
                reason);
    }
}
