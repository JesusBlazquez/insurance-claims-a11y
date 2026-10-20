package io.github.jesusblazquez.insurance.domain.pricing;

import io.github.jesusblazquez.insurance.domain.Coverage;
import io.github.jesusblazquez.insurance.domain.Deductible;
import java.util.Objects;

/** Everything the premium depends on, and nothing else. */
public record QuoteRequest(Coverage coverage, int driverAge, int licenceYears, int horsepower, Deductible deductible) {

    public QuoteRequest {
        Objects.requireNonNull(coverage, "coverage is required");
        Objects.requireNonNull(deductible, "deductible is required");
        if (driverAge < 18 || driverAge > 110) {
            throw new IllegalArgumentException("The driver age must be between 18 and 110, but was " + driverAge);
        }
        if (licenceYears < 0 || licenceYears > driverAge - 18) {
            throw new IllegalArgumentException(
                    "A driver aged %d cannot have held a licence for %d years".formatted(driverAge, licenceYears));
        }
        if (horsepower < 1 || horsepower > 1000) {
            throw new IllegalArgumentException("Horsepower must be between 1 and 1000, but was " + horsepower);
        }
    }
}
