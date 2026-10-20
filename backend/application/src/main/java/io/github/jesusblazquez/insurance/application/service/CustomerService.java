package io.github.jesusblazquez.insurance.application.service;

import io.github.jesusblazquez.insurance.application.exception.CustomerNotFoundException;
import io.github.jesusblazquez.insurance.application.exception.DuplicateNifException;
import io.github.jesusblazquez.insurance.application.port.in.CustomerQueries;
import io.github.jesusblazquez.insurance.application.port.in.CustomerView;
import io.github.jesusblazquez.insurance.application.port.in.RegisterCustomerCommand;
import io.github.jesusblazquez.insurance.application.port.in.RegisterCustomerUseCase;
import io.github.jesusblazquez.insurance.application.port.out.CustomerRepository;
import io.github.jesusblazquez.insurance.domain.Customer;
import io.github.jesusblazquez.insurance.domain.Email;
import io.github.jesusblazquez.insurance.domain.Nif;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CustomerService implements RegisterCustomerUseCase, CustomerQueries {

    private final CustomerRepository customers;
    private final Clock clock;

    public CustomerService(CustomerRepository customers, Clock clock) {
        this.customers = Objects.requireNonNull(customers);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public CustomerView register(RegisterCustomerCommand command) {
        Nif nif = Nif.of(command.nif());
        customers.findByNif(nif).ifPresent(existing -> {
            throw new DuplicateNifException(nif.value());
        });
        Customer customer = Customer.register(
                UUID.randomUUID(),
                nif,
                command.fullName(),
                command.birthDate(),
                Email.of(command.email()),
                command.phone(),
                LocalDate.now(clock));
        return CustomerView.of(customers.save(customer));
    }

    @Override
    public CustomerView byId(UUID id) {
        return customers.findById(id).map(CustomerView::of).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @Override
    public List<CustomerView> all() {
        return customers.findAll().stream().map(CustomerView::of).toList();
    }
}
