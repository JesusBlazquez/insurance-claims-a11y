package io.github.jesusblazquez.insurance.application.fake;

import io.github.jesusblazquez.insurance.application.port.out.PolicyNumbers;
import io.github.jesusblazquez.insurance.domain.PolicyNumber;
import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

/** Counts from one. The real implementation will use a database sequence. */
public class SequentialPolicyNumbers implements PolicyNumbers {

    private final AtomicInteger counter = new AtomicInteger();
    private final Year year;

    public SequentialPolicyNumbers(Year year) {
        this.year = year;
    }

    @Override
    public PolicyNumber next() {
        return PolicyNumber.sequential(year, counter.incrementAndGet());
    }
}
