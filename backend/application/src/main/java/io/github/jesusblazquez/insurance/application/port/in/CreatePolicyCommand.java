package io.github.jesusblazquez.insurance.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * What the caller sends to draft a policy.
 *
 * <p>There is no premium here on purpose: the price is worked out by the domain from the rest of
 * the figures. Letting the client send it would let the client choose it.
 */
public record CreatePolicyCommand(
        UUID customerId,
        String plate,
        String make,
        String model,
        int horsepower,
        int modelYear,
        String coverage,
        BigDecimal insuredSum,
        String deductible,
        int licenceYears,
        LocalDate validFrom) {}
