package io.github.jesusblazquez.insurance.application.port.in;

/** Drafts a policy, pricing it from the customer's age and the vehicle. */
public interface CreatePolicyUseCase {

    PolicyView create(CreatePolicyCommand command);
}
