package io.github.jesusblazquez.insurance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.application.exception.CustomerNotFoundException;
import io.github.jesusblazquez.insurance.application.exception.PolicyNotFoundException;
import io.github.jesusblazquez.insurance.application.fake.InMemoryCustomerRepository;
import io.github.jesusblazquez.insurance.application.fake.InMemoryPolicyRepository;
import io.github.jesusblazquez.insurance.application.fake.SequentialPolicyNumbers;
import io.github.jesusblazquez.insurance.application.port.in.CancelPolicyCommand;
import io.github.jesusblazquez.insurance.application.port.in.CreatePolicyCommand;
import io.github.jesusblazquez.insurance.application.port.in.PolicyView;
import io.github.jesusblazquez.insurance.application.port.in.QuoteCommand;
import io.github.jesusblazquez.insurance.application.port.in.QuoteView;
import io.github.jesusblazquez.insurance.application.port.in.RegisterCustomerCommand;
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

class PolicyServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate VALID_FROM = LocalDate.of(2026, 11, 1);

    private InMemoryPolicyRepository policies;
    private CustomerService customerService;
    private PolicyService service;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        policies = new InMemoryPolicyRepository();
        InMemoryCustomerRepository customers = new InMemoryCustomerRepository();
        customerService = new CustomerService(customers, CLOCK);
        service = new PolicyService(policies, customers, new SequentialPolicyNumbers(Year.of(2026)), CLOCK);
        customerId = customerService
                .register(new RegisterCustomerCommand(
                        "12345678Z", "Ana Gomez", LocalDate.of(1990, 5, 10), "ana@example.com", "600123456"))
                .id();
    }

    @Test
    @DisplayName("quotes a premium without creating anything")
    void quotesWithoutCreating() {
        QuoteView quote = service.quote(new QuoteCommand("COMPREHENSIVE", 36, 10, 110, "MEDIUM"));

        assertThat(quote.premium()).isEqualByComparingTo("513.00");
        assertThat(quote.currency()).isEqualTo("EUR");
        assertThat(policies.search(null, null)).isEmpty();
    }

    @Test
    @DisplayName("explains which values were allowed when the coverage is misspelled")
    void explainsAllowedCoverages() {
        assertThatThrownBy(() -> service.quote(new QuoteCommand("FULL_COVER", 36, 10, 110, "MEDIUM")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("THIRD_PARTY")
                .hasMessageContaining("COMPREHENSIVE");
    }

    @Test
    @DisplayName("drafts a policy and prices it from the customer on file")
    void draftsAPolicy() {
        PolicyView view = service.create(command());

        assertThat(view.status()).isEqualTo("DRAFT");
        assertThat(view.policyNumber()).isEqualTo("POL-2026-000001");
        assertThat(view.premium()).isEqualByComparingTo("513.00");
        assertThat(view.validTo()).isEqualTo(LocalDate.of(2027, 10, 31));
    }

    @Test
    @DisplayName("refuses to draft a policy for a customer who does not exist")
    void refusesAnUnknownCustomer() {
        CreatePolicyCommand orphan = new CreatePolicyCommand(
                UUID.randomUUID(),
                "1234BCD",
                "Seat",
                "Leon",
                110,
                2020,
                "COMPREHENSIVE",
                new BigDecimal("20000.00"),
                "MEDIUM",
                10,
                VALID_FROM);

        assertThatThrownBy(() -> service.create(orphan)).isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    @DisplayName("issues and then cancels a policy")
    void issuesAndCancels() {
        UUID id = service.create(command()).id();

        assertThat(service.issue(id).status()).isEqualTo("ACTIVE");

        PolicyView cancelled = service.cancel(new CancelPolicyCommand(id, "unpaid premium"));
        assertThat(cancelled.status()).isEqualTo("CANCELLED");
        assertThat(cancelled.cancelledOn()).isEqualTo(LocalDate.of(2026, 10, 20));
        assertThat(cancelled.cancellationReason()).isEqualTo("unpaid premium");
    }

    @Test
    @DisplayName("filters the listing by customer and by state")
    void filtersTheListing() {
        UUID id = service.create(command()).id();
        service.issue(id);

        assertThat(service.search(customerId, "ACTIVE")).hasSize(1);
        assertThat(service.search(customerId, "DRAFT")).isEmpty();
        assertThat(service.search(UUID.randomUUID(), null)).isEmpty();
    }

    @Test
    @DisplayName("complains about a policy that does not exist")
    void complainsAboutAnUnknownPolicy() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.issue(unknown)).isInstanceOf(PolicyNotFoundException.class);
    }

    private CreatePolicyCommand command() {
        return new CreatePolicyCommand(
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
                VALID_FROM);
    }
}
