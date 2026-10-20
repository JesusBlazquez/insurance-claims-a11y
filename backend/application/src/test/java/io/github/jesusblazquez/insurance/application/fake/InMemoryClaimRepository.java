package io.github.jesusblazquez.insurance.application.fake;

import io.github.jesusblazquez.insurance.application.port.out.ClaimRepository;
import io.github.jesusblazquez.insurance.domain.Claim;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryClaimRepository implements ClaimRepository {

    private final Map<UUID, Claim> stored = new LinkedHashMap<>();

    @Override
    public Claim save(Claim claim) {
        stored.put(claim.id(), claim);
        return claim;
    }

    @Override
    public Optional<Claim> findById(UUID id) {
        return Optional.ofNullable(stored.get(id));
    }

    @Override
    public List<Claim> search(UUID policyId, String status) {
        return stored.values().stream()
                .filter(c -> policyId == null || c.policyId().equals(policyId))
                .filter(c -> status == null || c.state().label().equals(status))
                .toList();
    }
}
