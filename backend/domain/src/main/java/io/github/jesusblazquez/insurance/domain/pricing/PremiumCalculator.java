package io.github.jesusblazquez.insurance.domain.pricing;

import io.github.jesusblazquez.insurance.domain.Money;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Works out the yearly premium for a car policy.
 *
 * <p>This is a pure function: same request in, same premium out, every time. It reads no clock and
 * no database, which is why it can be tested with a plain table of cases instead of a running
 * application.
 *
 * <p>The factors are multiplied together before touching the money, and the amount is rounded once
 * at the end. Rounding after every step would make the result depend on the order the factors were
 * applied in, which is the kind of bug nobody finds until an auditor does.
 *
 * <p>There is deliberately no floor price. With the factors below the cheapest possible policy
 * costs 243.00 EUR, so a minimum premium would be a branch no input could ever reach.
 */
public final class PremiumCalculator {

    private static final Money BASE = Money.euros("300.00");
    private static final BigDecimal ONE = BigDecimal.ONE;

    private PremiumCalculator() {}

    public static Money quote(QuoteRequest request) {
        Objects.requireNonNull(request, "request is required");

        BigDecimal factor = request.coverage()
                .pricingFactor()
                .multiply(ageFactor(request.driverAge()))
                .multiply(licenceFactor(request.licenceYears()))
                .multiply(horsepowerFactor(request.horsepower()))
                .multiply(ONE.subtract(request.deductible().discount()));

        return BASE.multiply(factor);
    }

    /** Young drivers crash more and the over-65s have slower reactions; the middle band is the norm. */
    private static BigDecimal ageFactor(int age) {
        if (age < 25) {
            return new BigDecimal("1.50");
        }
        return age >= 65 ? new BigDecimal("1.20") : ONE;
    }

    /** Years holding a licence matter more than age for a new driver. */
    private static BigDecimal licenceFactor(int years) {
        if (years < 2) {
            return new BigDecimal("1.30");
        }
        return years <= 5 ? new BigDecimal("1.10") : ONE;
    }

    private static BigDecimal horsepowerFactor(int horsepower) {
        if (horsepower <= 90) {
            return new BigDecimal("0.90");
        }
        return horsepower > 150 ? new BigDecimal("1.25") : ONE;
    }
}
