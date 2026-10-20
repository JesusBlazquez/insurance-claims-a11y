package io.github.jesusblazquez.insurance.application.port.in;

import java.util.UUID;

public record RejectClaimCommand(UUID claimId, String reason) {}
