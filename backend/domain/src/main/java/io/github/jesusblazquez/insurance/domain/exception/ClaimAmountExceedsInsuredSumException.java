package io.github.jesusblazquez.insurance.domain.exception;

/** Thrown when the amount claimed is higher than the sum insured by the policy. */
public class ClaimAmountExceedsInsuredSumException extends DomainException {

    public ClaimAmountExceedsInsuredSumException(String claimed, String insuredSum) {
        super("The claimed amount %s exceeds the insured sum %s".formatted(claimed, insuredSum));
    }
}
