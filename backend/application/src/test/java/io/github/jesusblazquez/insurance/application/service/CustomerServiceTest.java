package io.github.jesusblazquez.insurance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.application.exception.CustomerNotFoundException;
import io.github.jesusblazquez.insurance.application.exception.DuplicateNifException;
import io.github.jesusblazquez.insurance.application.fake.InMemoryCustomerRepository;
import io.github.jesusblazquez.insurance.application.port.in.CustomerView;
import io.github.jesusblazquez.insurance.application.port.in.RegisterCustomerCommand;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC);

    private InMemoryCustomerRepository customers;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        customers = new InMemoryCustomerRepository();
        service = new CustomerService(customers, CLOCK);
    }

    @Test
    @DisplayName("registers a customer and hands back what was stored")
    void registersACustomer() {
        CustomerView view = service.register(command("12345678Z"));

        assertThat(view.nif()).isEqualTo("12345678Z");
        assertThat(customers.findById(view.id())).isPresent();
    }

    @Test
    @DisplayName("refuses a NIF that is already registered")
    void refusesADuplicateNif() {
        service.register(command("12345678Z"));

        assertThatThrownBy(() -> service.register(command("12345678Z")))
                .isInstanceOf(DuplicateNifException.class)
                .hasMessageContaining("12345678Z");
    }

    @Test
    @DisplayName("complains about an identifier that does not exist")
    void complainsAboutAnUnknownCustomer() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.byId(unknown)).isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    @DisplayName("lists what has been registered")
    void listsCustomers() {
        service.register(command("12345678Z"));
        service.register(command("00000000T"));

        assertThat(service.all()).hasSize(2);
    }

    private static RegisterCustomerCommand command(String nif) {
        return new RegisterCustomerCommand(nif, "Ana Gomez", LocalDate.of(1990, 5, 10), "ana@example.com", "600123456");
    }
}
