package io.github.jesusblazquez.insurance.application.port.in;

/** Registers a new policy holder. */
public interface RegisterCustomerUseCase {

    CustomerView register(RegisterCustomerCommand command);
}
