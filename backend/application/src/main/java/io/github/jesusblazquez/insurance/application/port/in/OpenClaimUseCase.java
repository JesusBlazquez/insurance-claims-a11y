package io.github.jesusblazquez.insurance.application.port.in;

/** Files a claim against a policy, if that policy covered the vehicle on the day of the incident. */
public interface OpenClaimUseCase {

    ClaimView open(OpenClaimCommand command);
}
