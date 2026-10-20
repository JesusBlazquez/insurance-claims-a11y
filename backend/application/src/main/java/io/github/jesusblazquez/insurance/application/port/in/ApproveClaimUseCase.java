package io.github.jesusblazquez.insurance.application.port.in;

import java.util.UUID;

/** Approves a claim under review and works out what is payable. */
public interface ApproveClaimUseCase {

    ClaimView approve(UUID claimId);
}
