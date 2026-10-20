package io.github.jesusblazquez.insurance.application.port.in;

import io.github.jesusblazquez.insurance.domain.Customer;
import java.time.LocalDate;
import java.util.UUID;

/** What the application gives back about a customer. */
public record CustomerView(UUID id, String nif, String fullName, LocalDate birthDate, String email, String phone) {

    public static CustomerView of(Customer customer) {
        return new CustomerView(
                customer.id(),
                customer.nif().value(),
                customer.fullName(),
                customer.birthDate(),
                customer.email().value(),
                customer.phone());
    }
}
