package io.github.jesusblazquez.insurance.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OpenClaimCommand(UUID policyId, LocalDate occurredOn, String description, BigDecimal claimedAmount) {}
