package io.github.jesusblazquez.insurance.application.service;

import io.github.jesusblazquez.insurance.application.exception.ClaimNotFoundException;
import io.github.jesusblazquez.insurance.application.exception.PolicyNotFoundException;
import io.github.jesusblazquez.insurance.application.port.in.ApproveClaimUseCase;
import io.github.jesusblazquez.insurance.application.port.in.ClaimQueries;
import io.github.jesusblazquez.insurance.application.port.in.ClaimView;
import io.github.jesusblazquez.insurance.application.port.in.OpenClaimCommand;
import io.github.jesusblazquez.insurance.application.port.in.OpenClaimUseCase;
import io.github.jesusblazquez.insurance.application.port.in.PayClaimUseCase;
import io.github.jesusblazquez.insurance.application.port.in.RejectClaimCommand;
import io.github.jesusblazquez.insurance.application.port.in.RejectClaimUseCase;
import io.github.jesusblazquez.insurance.application.port.in.StartClaimReviewUseCase;
import io.github.jesusblazquez.insurance.application.port.out.ClaimNumbers;
import io.github.jesusblazquez.insurance.application.port.out.ClaimRepository;
import io.github.jesusblazquez.insurance.application.port.out.PolicyRepository;
import io.github.jesusblazquez.insurance.domain.Claim;
import io.github.jesusblazquez.insurance.domain.Money;
import io.github.jesusblazquez.insurance.domain.Policy;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClaimService
        implements OpenClaimUseCase,
                StartClaimReviewUseCase,
                ApproveClaimUseCase,
                RejectClaimUseCase,
                PayClaimUseCase,
                ClaimQueries {

    private final ClaimRepository claims;
    private final PolicyRepository policies;
    private final ClaimNumbers claimNumbers;
    private final Clock clock;

    public ClaimService(ClaimRepository claims, PolicyRepository policies, ClaimNumbers claimNumbers, Clock clock) {
        this.claims = Objects.requireNonNull(claims);
        this.policies = Objects.requireNonNull(policies);
        this.claimNumbers = Objects.requireNonNull(claimNumbers);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ClaimView open(OpenClaimCommand command) {
        Policy policy = policies.findById(command.policyId())
                .orElseThrow(() -> new PolicyNotFoundException(command.policyId()));

        // Today is the day the claim reaches us. The domain uses it to rule out incidents
        // dated in the future without ever reading a clock itself.
        Claim claim = Claim.open(
                UUID.randomUUID(),
                claimNumbers.next(),
                policy,
                command.occurredOn(),
                LocalDate.now(clock),
                command.description(),
                Money.of(command.claimedAmount(), Money.EUR));
        return ClaimView.of(claims.save(claim));
    }

    @Override
    public ClaimView startReview(UUID claimId) {
        Claim claim = require(claimId);
        claim.startReview(LocalDate.now(clock));
        return ClaimView.of(claims.save(claim));
    }

    @Override
    public ClaimView approve(UUID claimId) {
        Claim claim = require(claimId);
        claim.approve(LocalDate.now(clock));
        return ClaimView.of(claims.save(claim));
    }

    @Override
    public ClaimView reject(RejectClaimCommand command) {
        Claim claim = require(command.claimId());
        claim.reject(LocalDate.now(clock), command.reason());
        return ClaimView.of(claims.save(claim));
    }

    @Override
    public ClaimView pay(UUID claimId) {
        Claim claim = require(claimId);
        claim.pay(LocalDate.now(clock));
        return ClaimView.of(claims.save(claim));
    }

    @Override
    public ClaimView byId(UUID id) {
        return ClaimView.of(require(id));
    }

    @Override
    public List<ClaimView> search(UUID policyId, String status) {
        return claims.search(policyId, status).stream().map(ClaimView::of).toList();
    }

    private Claim require(UUID id) {
        return claims.findById(id).orElseThrow(() -> new ClaimNotFoundException(id));
    }
}
