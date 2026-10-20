package io.github.jesusblazquez.insurance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.domain.exception.UnderageCustomerException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    @Test
    @DisplayName("registers an adult policy holder")
    void registersAnAdult() {
        Customer customer = register(LocalDate.of(1995, 5, 10));

        assertThat(customer.ageOn(TODAY)).isEqualTo(31);
    }

    @Test
    @DisplayName("accepts someone who turns eighteen today")
    void acceptsSomeoneTurningEighteenToday() {
        Customer customer = register(TODAY.minusYears(18));

        assertThat(customer.ageOn(TODAY)).isEqualTo(18);
    }

    @Test
    @DisplayName("rejects someone who turns eighteen tomorrow")
    void rejectsSomeoneTurningEighteenTomorrow() {
        LocalDate birthDate = TODAY.minusYears(18).plusDays(1);

        assertThatThrownBy(() -> register(birthDate))
                .isInstanceOf(UnderageCustomerException.class)
                .hasMessageContaining("17");
    }

    @Test
    @DisplayName("rejects a phone number that is not nine digits")
    void rejectsBadPhone() {
        assertThatThrownBy(() -> new Customer(
                        UUID.randomUUID(),
                        Nif.of("12345678Z"),
                        "Ana Gomez",
                        LocalDate.of(1990, 1, 1),
                        Email.of("ana@example.com"),
                        "12345"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Customer register(LocalDate birthDate) {
        return Customer.register(
                UUID.randomUUID(),
                Nif.of("12345678Z"),
                "Ana Gomez",
                birthDate,
                Email.of("ana@example.com"),
                "600123456",
                TODAY);
    }
}
