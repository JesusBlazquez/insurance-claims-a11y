package io.github.jesusblazquez.insurance.application.port.in;

import java.util.List;
import java.util.UUID;

/** Read-only access to claims. Both filters are optional. */
public interface ClaimQueries {

    ClaimView byId(UUID id);

    List<ClaimView> search(UUID policyId, String status);
}
