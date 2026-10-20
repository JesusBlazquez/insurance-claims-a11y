package io.github.jesusblazquez.insurance.application.port.in;

/** Cancels a policy that is in force, from today onwards. */
public interface CancelPolicyUseCase {

    PolicyView cancel(CancelPolicyCommand command);
}
