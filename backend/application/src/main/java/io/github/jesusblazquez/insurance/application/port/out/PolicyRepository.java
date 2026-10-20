package io.github.jesusblazquez.insurance.application.port.out;

import io.github.jesusblazquez.insurance.domain.Policy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepository {

    Policy save(Policy policy);

    Optional<Policy> findById(UUID id);

    /** Both filters are optional; a null means "do not filter by this". */
    List<Policy> search(UUID customerId, String status);
}
