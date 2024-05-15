package eu.ill.visa.web.graphql.directives;

import eu.ill.visa.web.graphql.context.AuthenticationContext;
import eu.ill.visa.web.graphql.exceptions.UnauthorisedException;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.security.tokens.AccountToken;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

@ApplicationScoped
public class RoleDirective implements SchemaDirectiveWiring {

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
        final String targetAuthRole = environment.getAppliedDirective().getArgument("role").getValue();
        final GraphQLFieldDefinition field = environment.getElement();
        final GraphQLFieldsContainer parentType = environment.getFieldsContainer();
        final DataFetcher<?> originalDataFetcher = environment.getCodeRegistry().getDataFetcher(parentType, field);
        final DataFetcher<?> dataFetcher = dataFetchingEnvironment -> {
            final AuthenticationContext context = dataFetchingEnvironment.getContext();
            final AccountToken token = context.getAccountToken();

            if (token.getUser() != null) {
                return token.getUser().hasRole(targetAuthRole);
            }

            throw new UnauthorisedException("You are not authorised to access this resource");
        };

        environment.getCodeRegistry().dataFetcher(parentType, field, dataFetcher);
        return field;
    }
}

