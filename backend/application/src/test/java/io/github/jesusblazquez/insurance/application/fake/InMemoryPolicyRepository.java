package io.github.jesusblazquez.insurance.application.fake;

import io.github.jesusblazquez.insurance.application.port.out.PolicyRepository;
import io.github.jesusblazquez.insurance.domain.Policy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryPolicyRepository implements PolicyRepository {

    private final Map<UUID, Policy> stored = new LinkedHashMap<>();

    @Override
    public Policy save(Policy policy) {
        stored.put(policy.id(), policy);
        return policy;
    }

    @Override
    public Optional<Policy> findById(UUID id) {
        return Optional.ofNullable(stored.get(id));
    }

    @Override
    public List<Policy> search(UUID customerId, String status) {
        return stored.values().stream()
                .filter(p -> customerId == null || p.customerId().equals(customerId))
                .filter(p -> status == null || p.state().label().equals(status))
                .toList();
    }
}
