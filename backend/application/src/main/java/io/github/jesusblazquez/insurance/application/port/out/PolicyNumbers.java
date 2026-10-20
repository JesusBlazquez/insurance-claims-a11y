package io.github.jesusblazquez.insurance.application.port.out;

import io.github.jesusblazquez.insurance.domain.PolicyNumber;

/**
 * Hands out the next policy number.
 *
 * <p>A port rather than a utility because the only correct implementation needs a database
 * sequence: two users buying a policy at the same instant must not get the same number.
 */
public interface PolicyNumbers {

    PolicyNumber next();
}
