package io.github.jesusblazquez.insurance.application.port.in;

import java.math.BigDecimal;

/** The price, and the figures it was worked out from, so the caller can show its reasoning. */
public record QuoteView(BigDecimal premium, String currency, String coverage, String deductible) {}
