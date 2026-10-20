package io.github.jesusblazquez.insurance.application.port.in;

/** Everything needed to price a policy, without creating anything. */
public record QuoteCommand(String coverage, int driverAge, int licenceYears, int horsepower, String deductible) {}
