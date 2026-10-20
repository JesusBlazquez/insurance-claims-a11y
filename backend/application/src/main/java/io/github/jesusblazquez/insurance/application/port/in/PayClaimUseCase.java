package io.github.jesusblazquez.insurance.application.port.in;

import java.util.UUID;

/** Pays an approved claim. */
public interface PayClaimUseCase {

    ClaimView pay(UUID claimId);
}
