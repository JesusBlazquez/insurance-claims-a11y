package io.github.jesusblazquez.insurance.application.exception;

import java.util.UUID;

public class CustomerNotFoundException extends NotFoundException {

    public CustomerNotFoundException(UUID id) {
        super("Customer", id);
    }
}
