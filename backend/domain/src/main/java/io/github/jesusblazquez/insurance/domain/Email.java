package io.github.jesusblazquez.insurance.domain;

import io.github.jesusblazquez.insurance.domain.exception.InvalidEmailException;
import java.util.regex.Pattern;

/**
 * An email address.
 *
 * <p>The check is deliberately loose. Validating email with a strict regular expression is a known
 * trap: the real grammar allows far more than people expect, and over-strict patterns reject valid
 * addresses. The only way to know an address works is to send to it.
 */
public record Email(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    public Email {
        if (value == null) {
            throw new InvalidEmailException("null");
        }
        value = value.trim().toLowerCase();
        if (value.length() > 254 || !FORMAT.matcher(value).matches()) {
            throw new InvalidEmailException(value);
        }
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
