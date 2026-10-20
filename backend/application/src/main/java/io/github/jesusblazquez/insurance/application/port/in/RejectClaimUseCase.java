package io.github.jesusblazquez.insurance.application.port.in;

/** Rejects a claim under review, with a reason the customer can be told. */
public interface RejectClaimUseCase {

    ClaimView reject(RejectClaimCommand command);
}
