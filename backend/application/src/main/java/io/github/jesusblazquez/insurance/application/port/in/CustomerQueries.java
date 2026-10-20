package io.github.jesusblazquez.insurance.application.port.in;

import java.util.List;
import java.util.UUID;

/** Read-only access to customers. Separated from the commands: reads and writes age differently. */
public interface CustomerQueries {

    CustomerView byId(UUID id);

    List<CustomerView> all();
}
