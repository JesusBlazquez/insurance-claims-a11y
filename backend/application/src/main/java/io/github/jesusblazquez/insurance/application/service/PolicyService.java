package io.github.jesusblazquez.insurance.application.service;

import io.github.jesusblazquez.insurance.application.exception.CustomerNotFoundException;
import io.github.jesusblazquez.insurance.application.exception.PolicyNotFoundException;
import io.github.jesusblazquez.insurance.application.port.in.CancelPolicyCommand;
import io.github.jesusblazquez.insurance.application.port.in.CancelPolicyUseCase;
import io.github.jesusblazquez.insurance.application.port.in.CreatePolicyCommand;
import io.github.jesusblazquez.insurance.application.port.in.CreatePolicyUseCase;
import io.github.jesusblazquez.insurance.application.port.in.IssuePolicyUseCase;
import io.github.jesusblazquez.insurance.application.port.in.PolicyQueries;
import io.github.jesusblazquez.insurance.application.port.in.PolicyView;
import io.github.jesusblazquez.insurance.application.port.in.QuoteCommand;
import io.github.jesusblazquez.insurance.application.port.in.QuotePremiumUseCase;
import io.github.jesusblazquez.insurance.application.port.in.QuoteView;
import io.github.jesusblazquez.insurance.application.port.out.CustomerRepository;
import io.github.jesusblazquez.insurance.application.port.out.PolicyNumbers;
import io.github.jesusblazquez.insurance.application.port.out.PolicyRepository;
import io.github.jesusblazquez.insurance.domain.Coverage;
import io.github.jesusblazquez.insurance.domain.Customer;
import io.github.jesusblazquez.insurance.domain.Deductible;
import io.github.jesusblazquez.insurance.domain.Money;
import io.github.jesusblazquez.insurance.domain.PlateNumber;
import io.github.jesusblazquez.insurance.domain.Policy;
import io.github.jesusblazquez.insurance.domain.Vehicle;
import io.github.jesusblazquez.insurance.domain.pricing.PremiumCalculator;
import io.github.jesusblazquez.insurance.domain.pricing.QuoteRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PolicyService
        implements QuotePremiumUseCase, CreatePolicyUseCase, IssuePolicyUseCase, CancelPolicyUseCase, PolicyQueries {

    private final PolicyRepository policies;
    private final CustomerRepository customers;
    private final PolicyNumbers policyNumbers;
    private final Clock clock;

    public PolicyService(
            PolicyRepository policies, CustomerRepository customers, PolicyNumbers policyNumbers, Clock clock) {
        this.policies = Objects.requireNonNull(policies);
        this.customers = Objects.requireNonNull(customers);
        this.policyNumbers = Objects.requireNonNull(policyNumbers);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public QuoteView quote(QuoteCommand command) {
        Coverage coverage = Enums.parse(Coverage.class, command.coverage(), "coverage");
        Deductible deductible = Enums.parse(Deductible.class, command.deductible(), "deductible");
        Money premium = PremiumCalculator.quote(new QuoteRequest(
                coverage, command.driverAge(), command.licenceYears(), command.horsepower(), deductible));
        return new QuoteView(
                premium.amount(), premium.currency().getCurrencyCode(), coverage.name(), deductible.name());
    }

    @Override
    public PolicyView create(CreatePolicyCommand command) {
        Customer customer = customers
                .findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));
        Coverage coverage = Enums.parse(Coverage.class, command.coverage(), "coverage");
        Deductible deductible = Enums.parse(Deductible.class, command.deductible(), "deductible");

        Vehicle vehicle = new Vehicle(
                PlateNumber.of(command.plate()),
                command.make(),
                command.model(),
                command.horsepower(),
                command.modelYear());

        // The driver's age is taken from the customer on file, never from the request:
        // otherwise anyone could buy a cheaper policy by claiming to be forty.
        int driverAge = customer.ageOn(command.validFrom());
        Money premium = PremiumCalculator.quote(
                new QuoteRequest(coverage, driverAge, command.licenceYears(), command.horsepower(), deductible));

        Policy policy = Policy.draft(
                UUID.randomUUID(),
                policyNumbers.next(),
                customer.id(),
                vehicle,
                coverage,
                Money.of(command.insuredSum(), Money.EUR),
                deductible,
                command.validFrom(),
                command.validFrom().plusYears(1).minusDays(1),
                premium);
        return PolicyView.of(policies.save(policy));
    }

    @Override
    public PolicyView issue(UUID policyId) {
        Policy policy = require(policyId);
        policy.issue(LocalDate.now(clock));
        return PolicyView.of(policies.save(policy));
    }

    @Override
    public PolicyView cancel(CancelPolicyCommand command) {
        Policy policy = require(command.policyId());
        policy.cancel(LocalDate.now(clock), command.reason());
        return PolicyView.of(policies.save(policy));
    }

    @Override
    public PolicyView byId(UUID id) {
        return PolicyView.of(require(id));
    }

    @Override
    public List<PolicyView> search(UUID customerId, String status) {
        return policies.search(customerId, status).stream().map(PolicyView::of).toList();
    }

    private Policy require(UUID id) {
        return policies.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
    }
}
