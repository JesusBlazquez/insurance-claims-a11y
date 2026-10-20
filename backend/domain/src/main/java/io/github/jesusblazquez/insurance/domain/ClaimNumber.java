package io.github.jesusblazquez.insurance.domain;

import java.time.Year;
import java.util.regex.Pattern;

/** A claim number in the format {@code CLM-YYYY-NNNNNN}. */
public record ClaimNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^CLM-\\d{4}-\\d{6}$");

    public ClaimNumber {
        if (value == null || !FORMAT.matcher(value.trim().toUpperCase()).matches()) {
            throw new IllegalArgumentException("'%s' is not a valid claim number".formatted(value));
        }
        value = value.trim().toUpperCase();
    }

    public static ClaimNumber of(String value) {
        return new ClaimNumber(value);
    }

    public static ClaimNumber sequential(Year year, int sequence) {
        return new ClaimNumber("CLM-%d-%06d".formatted(year.getValue(), sequence));
    }

    @Override
    public String toString() {
        return value;
    }
}
