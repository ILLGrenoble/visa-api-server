package eu.ill.visa.web.graphql.exceptions;

import eu.ill.visa.web.graphql.validation.ValidationError;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphQLException;
import graphql.language.SourceLocation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.ErrorType.ValidationError;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

public class ValidationException extends GraphQLException implements GraphQLError {

    private static final long serialVersionUID = 1L;

    private final Set<ConstraintViolation<?>> violations;

    public ValidationException(Throwable throwable) {
        super(throwable);
        final ConstraintViolationException violationException = (ConstraintViolationException) throwable;
        this.violations = violationException.getConstraintViolations();
    }

    @Override
    public String getMessage() {
        return "There was an error validating the input";
    }

    @Override
    public List<SourceLocation> getLocations() {
        return emptyList();
    }

    @Override
    public ErrorType getErrorType() {
        return ValidationError;
    }

    public ValidationError createError(final ConstraintViolation<?> violation) {
        final String property = violation.getPropertyPath().toString();
        final String message = violation.getMessage();
        return new ValidationError(property, message);
    }

    @Override
    public Map<String, Object> getExtensions() {
        return singletonMap(
            "errors",
            violations.stream().map(this::createError)
        );
    }

}
