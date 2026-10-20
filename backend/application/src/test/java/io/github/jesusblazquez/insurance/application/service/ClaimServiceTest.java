package io.github.jesusblazquez.insurance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.application.exception.ClaimNotFoundException;
import io.github.jesusblazquez.insurance.application.exception.PolicyNotFoundException;
import io.github.jesusblazquez.insurance.application.fake.InMemoryClaimRepository;
import io.github.jesusblazquez.insurance.application.fake.InMemoryCustomerRepository;
import io.github.jesusblazquez.insurance.application.fake.InMemoryPolicyRepository;
import io.github.jesusblazquez.insurance.application.fake.SequentialClaimNumbers;
import io.github.jesusblazquez.insurance.application.fake.SequentialPolicyNumbers;
import io.github.jesusblazquez.insurance.application.port.in.ClaimView;
import io.github.jesusblazquez.insurance.application.port.in.CreatePolicyCommand;
import io.github.jesusblazquez.insurance.application.port.in.OpenClaimCommand;
import io.github.jesusblazquez.insurance.application.port.in.RegisterCustomerCommand;
import io.github.jesusblazquez.insurance.application.port.in.RejectClaimCommand;
import io.github.jesusblazquez.insurance.domain.exception.PolicyNotInForceException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClaimServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);
    private static final LocalDate CRASH_DAY = LocalDate.of(2026, 6, 1);

    private ClaimService service;
    private PolicyService policyService;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        InMemoryCustomerRepository customers = new InMemoryCustomerRepository();
        InMemoryPolicyRepository policies = new InMemoryPolicyRepository();
        InMemoryClaimRepository claims = new InMemoryClaimRepository();
        CustomerService customerService = new CustomerService(customers, CLOCK);
        policyService = new PolicyService(policies, customers, new SequentialPolicyNumbers(Year.of(2026)), CLOCK);
        service = new ClaimService(claims, policies, new SequentialClaimNumbers(Year.of(2026)), CLOCK);

        UUID customerId = customerService
                .register(new RegisterCustomerCommand(
                        "12345678Z", "Ana Gomez", LocalDate.of(1990, 5, 10), "ana@example.com", "600123456"))
                .id();
        policyId = policyService
                .create(new CreatePolicyCommand(
                        customerId,
                        "1234BCD",
                        "Seat",
                        "Leon",
                        110,
                        2020,
                        "COMPREHENSIVE",
                        new BigDecimal("20000.00"),
                        "MEDIUM",
                        10,
                        LocalDate.of(2026, 1, 1)))
                .id();
        policyService.issue(policyId);
    }

    @Test
    @DisplayName("files a claim and dates it today")
    void filesAClaim() {
        ClaimView view = open("2000.00");

        assertThat(view.status()).isEqualTo("OPEN");
        assertThat(view.claimNumber()).isEqualTo("CLM-2026-000001");
        assertThat(view.occurredOn()).isEqualTo(CRASH_DAY);
        assertThat(view.reportedOn()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("refuses a claim against a policy that does not exist")
    void refusesAnUnknownPolicy() {
        OpenClaimCommand orphan = new OpenClaimCommand(
                UUID.randomUUID(), CRASH_DAY, "Rear-ended at a roundabout", new BigDecimal("500.00"));

        assertThatThrownBy(() -> service.open(orphan)).isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    @DisplayName("lets the domain refuse an incident the policy did not cover")
    void letsTheDomainRefuseAnUncoveredIncident() {
        OpenClaimCommand tooEarly = new OpenClaimCommand(
                policyId, LocalDate.of(2025, 12, 31), "Happened before the cover started", new BigDecimal("500.00"));

        assertThatThrownBy(() -> service.open(tooEarly)).isInstanceOf(PolicyNotInForceException.class);
    }

    @Test
    @DisplayName("runs a claim from filing to payment, taking the excess off")
    void settlesAClaim() {
        UUID claimId = open("2000.00").id();

        assertThat(service.startReview(claimId).status()).isEqualTo("UNDER_REVIEW");

        ClaimView approved = service.approve(claimId);
        assertThat(approved.status()).isEqualTo("APPROVED");
        assertThat(approved.settledAmount()).isEqualByComparingTo("1700.00");
        assertThat(approved.resolvedOn()).isEqualTo(TODAY);

        assertThat(service.pay(claimId).status()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("records the reason when a claim is rejected")
    void rejectsAClaim() {
        UUID claimId = open("2000.00").id();
        service.startReview(claimId);

        ClaimView rejected = service.reject(new RejectClaimCommand(claimId, "damage predates the cover"));

        assertThat(rejected.status()).isEqualTo("REJECTED");
        assertThat(rejected.rejectionReason()).isEqualTo("damage predates the cover");
        assertThat(rejected.settledAmount()).isNull();
    }

    @Test
    @DisplayName("filters the listing by policy and by state")
    void filtersTheListing() {
        UUID claimId = open("2000.00").id();
        service.startReview(claimId);

        assertThat(service.search(policyId, "UNDER_REVIEW")).hasSize(1);
        assertThat(service.search(policyId, "PAID")).isEmpty();
        assertThat(service.search(UUID.randomUUID(), null)).isEmpty();
    }

    @Test
    @DisplayName("complains about a claim that does not exist")
    void complainsAboutAnUnknownClaim() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.approve(unknown)).isInstanceOf(ClaimNotFoundException.class);
    }

    private ClaimView open(String amount) {
        return service.open(new OpenClaimCommand(
                policyId, CRASH_DAY, "Rear-ended at a roundabout, bumper damaged", new BigDecimal(amount)));
    }
}
