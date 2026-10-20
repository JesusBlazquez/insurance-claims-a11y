package io.github.jesusblazquez.insurance.application.port.in;

import java.util.UUID;

/** Puts a drafted policy in force. */
public interface IssuePolicyUseCase {

    PolicyView issue(UUID policyId);
}
