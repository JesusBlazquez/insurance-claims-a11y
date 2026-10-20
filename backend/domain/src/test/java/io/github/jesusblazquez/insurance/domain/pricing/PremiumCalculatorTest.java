package io.github.jesusblazquez.insurance.domain.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.domain.Coverage;
import io.github.jesusblazquez.insurance.domain.Deductible;
import io.github.jesusblazquez.insurance.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PremiumCalculatorTest {

    @ParameterizedTest(name = "{0}")
    @DisplayName("prices a policy from the published factors")
    @CsvSource({
        // description, coverage, age, licence years, hp, deductible, expected premium
        "experienced driver with comprehensive cover, COMPREHENSIVE, 30, 8, 110, MEDIUM, 513.00",
        "young driver with a new licence, THIRD_PARTY, 22, 1, 80, NONE, 526.50",
        "senior driver with extended cover, THIRD_PARTY_PLUS, 70, 40, 100, HIGH, 421.20",
        "cheapest possible policy, THIRD_PARTY, 40, 10, 80, HIGH, 243.00",
        "powerful car with comprehensive cover, COMPREHENSIVE, 45, 20, 200, NONE, 675.00"
    })
    void pricesFromTheFactors(
            String description,
            Coverage coverage,
            int age,
            int licenceYears,
            int horsepower,
            Deductible deductible,
            String expected) {
        QuoteRequest request = new QuoteRequest(coverage, age, licenceYears, horsepower, deductible);

        assertThat(PremiumCalculator.quote(request)).isEqualTo(Money.euros(expected));
    }

    @Test
    @DisplayName("is a pure function: the same request always yields the same premium")
    void isDeterministic() {
        QuoteRequest request = new QuoteRequest(Coverage.COMPREHENSIVE, 30, 8, 110, Deductible.MEDIUM);

        assertThat(PremiumCalculator.quote(request)).isEqualTo(PremiumCalculator.quote(request));
    }

    @Test
    @DisplayName("refuses a licence older than the driver could legally hold")
    void refusesImpossibleLicence() {
        assertThatThrownBy(() -> new QuoteRequest(Coverage.THIRD_PARTY, 20, 10, 100, Deductible.NONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("refuses a driver under eighteen")
    void refusesUnderageDriver() {
        assertThatThrownBy(() -> new QuoteRequest(Coverage.THIRD_PARTY, 17, 0, 100, Deductible.NONE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
