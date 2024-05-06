package eu.ill.visa.web.graphql.instrumentation;

import eu.ill.visa.web.graphql.context.AuthenticationContext;
import graphql.execution.AbortExecutionException;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationValidationParameters;
import graphql.validation.ValidationError;

import java.util.List;
import java.util.Optional;

public class AuthenticationInstrumentation extends SimpleInstrumentation {


    private boolean isUserPresent(AuthenticationContext context) {
        return Optional.ofNullable(context)
            .flatMap(ctx -> Optional.ofNullable(ctx.getAccountToken()))
            .isPresent();
    }

    @Override
    public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters) {
        final AuthenticationContext context = parameters.getContext();
        if (!isUserPresent(context)) {
            throw new AbortExecutionException("You must be authenticated");
        }
        return new SimpleInstrumentationContext<>();
    }


}
