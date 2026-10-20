package io.github.jesusblazquez.insurance.application.exception;

import java.util.UUID;

public class ClaimNotFoundException extends NotFoundException {

    public ClaimNotFoundException(UUID id) {
        super("Claim", id);
    }
}
