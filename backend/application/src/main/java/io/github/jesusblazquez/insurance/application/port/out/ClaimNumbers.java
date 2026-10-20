package io.github.jesusblazquez.insurance.application.port.out;

import io.github.jesusblazquez.insurance.domain.ClaimNumber;

/** Hands out the next claim number. See {@link PolicyNumbers} for why this is a port. */
public interface ClaimNumbers {

    ClaimNumber next();
}
