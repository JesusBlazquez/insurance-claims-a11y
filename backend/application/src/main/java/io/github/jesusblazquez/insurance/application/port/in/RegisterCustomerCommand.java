package io.github.jesusblazquez.insurance.application.port.in;

import java.time.LocalDate;

/** What the caller sends to register a customer. Plain data: no domain types cross this boundary. */
public record RegisterCustomerCommand(String nif, String fullName, LocalDate birthDate, String email, String phone) {}
