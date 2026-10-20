package io.github.jesusblazquez.insurance.domain;

import io.github.jesusblazquez.insurance.domain.exception.InvalidPlateNumberException;
import java.util.regex.Pattern;

/**
 * A Spanish plate number issued under the format in force since 2000: four digits and three
 * letters.
 *
 * <p>Vowels are excluded so that no plate spells a word, and Q, CH, LL and Ñ are excluded because
 * they are easy to confuse with other characters. Normalising to upper case without the separator
 * means {@code 1234 BCD} and {@code 1234-bcd} are the same plate, which is what a user expects.
 */
public record PlateNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");

    public PlateNumber {
        if (value == null) {
            throw new InvalidPlateNumberException("null");
        }
        value = value.trim().toUpperCase().replace(" ", "").replace("-", "");
        if (!FORMAT.matcher(value).matches()) {
            throw new InvalidPlateNumberException(value);
        }
    }

    public static PlateNumber of(String value) {
        return new PlateNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
