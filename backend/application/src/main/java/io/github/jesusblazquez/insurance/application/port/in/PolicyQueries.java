package io.github.jesusblazquez.insurance.application.port.in;

import java.util.List;
import java.util.UUID;

/** Read-only access to policies. Both filters are optional. */
public interface PolicyQueries {

    PolicyView byId(UUID id);

    List<PolicyView> search(UUID customerId, String status);
}
