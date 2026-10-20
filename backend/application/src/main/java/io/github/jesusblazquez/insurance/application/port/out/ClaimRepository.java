package io.github.jesusblazquez.insurance.application.port.out;

import io.github.jesusblazquez.insurance.domain.Claim;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository {

    /**
     * Stores the claim and whatever transition it has just gone through.
     *
     * <p>The adapter is responsible for appending the matching row to the event table and for
     * keeping the denormalised status in step, both in the same transaction.
     */
    Claim save(Claim claim);

    Optional<Claim> findById(UUID id);

    List<Claim> search(UUID policyId, String status);
}
