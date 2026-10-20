package io.github.jesusblazquez.insurance.application.port.out;

import io.github.jesusblazquez.insurance.domain.Customer;
import io.github.jesusblazquez.insurance.domain.Nif;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** How the application stores and finds customers. Implemented in the infrastructure module. */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByNif(Nif nif);

    List<Customer> findAll();
}
