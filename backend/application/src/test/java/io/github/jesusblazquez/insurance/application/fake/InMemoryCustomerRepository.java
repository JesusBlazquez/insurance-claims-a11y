package io.github.jesusblazquez.insurance.application.fake;

import io.github.jesusblazquez.insurance.application.port.out.CustomerRepository;
import io.github.jesusblazquez.insurance.domain.Customer;
import io.github.jesusblazquez.insurance.domain.Nif;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** A repository that lives in a map, so the use cases can be tested without a database. */
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<UUID, Customer> stored = new LinkedHashMap<>();

    @Override
    public Customer save(Customer customer) {
        stored.put(customer.id(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return Optional.ofNullable(stored.get(id));
    }

    @Override
    public Optional<Customer> findByNif(Nif nif) {
        return stored.values().stream().filter(c -> c.nif().equals(nif)).findFirst();
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(stored.values());
    }
}
