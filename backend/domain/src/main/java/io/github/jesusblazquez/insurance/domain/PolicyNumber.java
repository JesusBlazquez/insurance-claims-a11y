package io.github.jesusblazquez.insurance.domain;

import java.time.Year;
import java.util.regex.Pattern;

/** A policy number in the format {@code POL-YYYY-NNNNNN}. */
public record PolicyNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^POL-\\d{4}-\\d{6}$");

    public PolicyNumber {
        if (value == null || !FORMAT.matcher(value.trim().toUpperCase()).matches()) {
            throw new IllegalArgumentException("'%s' is not a valid policy number".formatted(value));
        }
        value = value.trim().toUpperCase();
    }

    public static PolicyNumber of(String value) {
        return new PolicyNumber(value);
    }

    public static PolicyNumber sequential(Year year, int sequence) {
        return new PolicyNumber("POL-%d-%06d".formatted(year.getValue(), sequence));
    }

    @Override
    public String toString() {
        return value;
    }
}
