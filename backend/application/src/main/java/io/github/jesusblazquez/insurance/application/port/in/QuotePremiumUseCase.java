package io.github.jesusblazquez.insurance.application.port.in;

/** Prices a policy without creating anything. */
public interface QuotePremiumUseCase {

    QuoteView quote(QuoteCommand command);
}
