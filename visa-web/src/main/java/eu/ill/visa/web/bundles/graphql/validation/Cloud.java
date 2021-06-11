package eu.ill.visa.web.bundles.graphql.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CloudValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cloud {
    String type();

    String message() default "Invalid cloud identifier";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
