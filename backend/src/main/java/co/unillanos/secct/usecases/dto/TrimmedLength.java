package co.unillanos.secct.usecases.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation constraint to verify character length after trimming leading and trailing whitespace (PL-2.05, PL-2.06).
 */
@Documented
@Constraint(validatedBy = TrimmedLengthValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TrimmedLength {

    int min() default 0;

    int max() default Integer.MAX_VALUE;

    String message() default "Longitud inválida tras recortar espacios";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
