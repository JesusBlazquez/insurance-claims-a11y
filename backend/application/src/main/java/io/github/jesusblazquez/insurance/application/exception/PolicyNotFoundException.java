package io.github.jesusblazquez.insurance.application.exception;

import java.util.UUID;

public class PolicyNotFoundException extends NotFoundException {

    public PolicyNotFoundException(UUID id) {
        super("Policy", id);
    }
}
