package io.github.jesusblazquez.insurance.application.port.in;

import java.util.UUID;

public record CancelPolicyCommand(UUID policyId, String reason) {}
