package io.github.jesusblazquez.insurance.domain;

import io.github.jesusblazquez.insurance.domain.exception.InvalidNifException;
import java.util.regex.Pattern;

/**
 * A Spanish tax identification number: eight digits plus a check letter.
 *
 * <p>The letter is not decorative. It is the remainder of dividing the number by 23, looked up in a
 * fixed table, so a typo in any digit almost always produces an invalid NIF. Validating it here
 * means no other layer has to wonder whether a NIF it receives is real.
 */
public record Nif(String value) {

    private static final Pattern FORMAT = Pattern.compile("^\\d{8}[A-Z]$");
    private static final String CHECK_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    public Nif {
        if (value == null) {
            throw new InvalidNifException("null");
        }
        value = value.trim().toUpperCase();
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidNifException(value);
        }
        int number = Integer.parseInt(value.substring(0, 8));
        char expected = CHECK_LETTERS.charAt(number % 23);
        if (value.charAt(8) != expected) {
            throw new InvalidNifException(value);
        }
    }

    public static Nif of(String value) {
        return new Nif(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
