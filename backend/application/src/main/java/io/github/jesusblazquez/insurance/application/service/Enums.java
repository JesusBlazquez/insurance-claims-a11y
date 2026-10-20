package io.github.jesusblazquez.insurance.application.service;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Turns the text that arrives from outside into an enum constant.
 *
 * <p>{@code Enum.valueOf} throws a message that names the Java class, which is useless to whoever
 * typed the wrong value. This one lists what was allowed.
 */
final class Enums {

    private Enums() {}

    static <E extends Enum<E>> E parse(Class<E> type, String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("%s is required".formatted(field));
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException cause) {
            String allowed =
                    Arrays.stream(type.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "'%s' is not a valid %s. Allowed values: %s".formatted(value, field, allowed));
        }
    }
}
