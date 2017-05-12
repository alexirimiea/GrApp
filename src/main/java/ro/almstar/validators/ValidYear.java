package ro.almstar.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Linking the validator I had shown above.
 * This constraint annotation can be used only on fields and method parameters.
 */
@Constraint(validatedBy = {YearValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface ValidYear {

    //The message to return when the instance of Complaint fails the validation.
    String message() default "Invalid year - must be greater than 1900";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}