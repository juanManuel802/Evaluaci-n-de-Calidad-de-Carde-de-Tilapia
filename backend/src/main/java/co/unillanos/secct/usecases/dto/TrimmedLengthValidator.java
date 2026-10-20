package co.unillanos.secct.usecases.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementation of TrimmedLength validator.
 */
public class TrimmedLengthValidator implements ConstraintValidator<TrimmedLength, String> {

    private int min;
    private int max;

    @Override
    public void initialize(TrimmedLength constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String trimmed = value.trim();
        return trimmed.length() >= min && trimmed.length() <= max;
    }
}
