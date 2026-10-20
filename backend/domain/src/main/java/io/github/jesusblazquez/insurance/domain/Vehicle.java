package io.github.jesusblazquez.insurance.domain;

import java.util.Objects;

/** The insured car. */
public record Vehicle(PlateNumber plate, String make, String model, int horsepower, int modelYear) {

    public Vehicle {
        Objects.requireNonNull(plate, "plate is required");
        make = requireText(make, "make");
        model = requireText(model, "model");
        if (horsepower < 1 || horsepower > 1000) {
            throw new IllegalArgumentException("Horsepower must be between 1 and 1000, but was " + horsepower);
        }
        if (modelYear < 1900 || modelYear > 2100) {
            throw new IllegalArgumentException("Model year must be between 1900 and 2100, but was " + modelYear);
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The vehicle %s is required".formatted(field));
        }
        return value.trim();
    }
}
