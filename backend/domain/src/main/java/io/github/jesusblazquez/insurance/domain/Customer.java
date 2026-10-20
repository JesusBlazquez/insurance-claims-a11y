package io.github.jesusblazquez.insurance.domain;

import io.github.jesusblazquez.insurance.domain.exception.UnderageCustomerException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Someone who can hold a policy.
 *
 * <p>The age check lives in the {@link #register} factory rather than in the constructor because it
 * needs to know what day it is, and a value object that reads the system clock cannot be tested
 * without freezing time. Passing the date in keeps the rule explicit and the test trivial.
 */
public record Customer(UUID id, Nif nif, String fullName, LocalDate birthDate, Email email, String phone) {

    private static final int MINIMUM_AGE = 18;
    private static final Pattern PHONE = Pattern.compile("^\\d{9}$");

    public Customer {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(nif, "nif is required");
        Objects.requireNonNull(email, "email is required");
        Objects.requireNonNull(birthDate, "birth date is required");
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("The full name is required");
        }
        fullName = fullName.trim();
        if (fullName.length() < 2 || fullName.length() > 120) {
            throw new IllegalArgumentException("The full name must be between 2 and 120 characters");
        }
        if (phone == null || !PHONE.matcher(phone.trim()).matches()) {
            throw new IllegalArgumentException("The phone number must be nine digits");
        }
        phone = phone.trim();
    }

    /** Registers a customer, refusing anyone under eighteen on the given day. */
    public static Customer register(
            UUID id, Nif nif, String fullName, LocalDate birthDate, Email email, String phone, LocalDate today) {
        Objects.requireNonNull(today, "today is required");
        Customer customer = new Customer(id, nif, fullName, birthDate, email, phone);
        int age = customer.ageOn(today);
        if (age < MINIMUM_AGE) {
            throw new UnderageCustomerException(age);
        }
        return customer;
    }

    public int ageOn(LocalDate date) {
        return Period.between(birthDate, date).getYears();
    }
}
