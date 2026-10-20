package io.github.jesusblazquez.insurance.domain;

import java.math.BigDecimal;

/** The three cover levels sold for car insurance, each with its pricing factor. */
public enum Coverage {
    /** Third party only: the legal minimum. */
    THIRD_PARTY(new BigDecimal("1.00")),
    /** Third party plus theft, fire and windscreen. */
    THIRD_PARTY_PLUS(new BigDecimal("1.30")),
    /** Comprehensive: own damage included. */
    COMPREHENSIVE(new BigDecimal("1.80"));

    private final BigDecimal pricingFactor;

    Coverage(BigDecimal pricingFactor) {
        this.pricingFactor = pricingFactor;
    }

    public BigDecimal pricingFactor() {
        return pricingFactor;
    }
}
