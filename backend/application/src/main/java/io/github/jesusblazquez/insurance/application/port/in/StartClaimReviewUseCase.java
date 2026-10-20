package io.github.jesusblazquez.insurance.application.port.in;

import java.util.UUID;

/** Takes a claim into review. */
public interface StartClaimReviewUseCase {

    ClaimView startReview(UUID claimId);
}
