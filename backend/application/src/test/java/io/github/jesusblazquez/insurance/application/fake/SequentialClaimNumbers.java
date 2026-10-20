package io.github.jesusblazquez.insurance.application.fake;

import io.github.jesusblazquez.insurance.application.port.out.ClaimNumbers;
import io.github.jesusblazquez.insurance.domain.ClaimNumber;
import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

/** Counts from one. The real implementation will use a database sequence. */
public class SequentialClaimNumbers implements ClaimNumbers {

    private final AtomicInteger counter = new AtomicInteger();
    private final Year year;

    public SequentialClaimNumbers(Year year) {
        this.year = year;
    }

    @Override
    public ClaimNumber next() {
        return ClaimNumber.sequential(year, counter.incrementAndGet());
    }
}
