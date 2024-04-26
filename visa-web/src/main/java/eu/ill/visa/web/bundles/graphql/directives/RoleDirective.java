package eu.ill.visa.web.bundles.graphql.directives;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.security.authorizers.ApplicationAuthorizer;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.bundles.graphql.context.AuthenticationContext;
import eu.ill.visa.web.bundles.graphql.exceptions.UnauthorisedException;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

@ApplicationScoped
public class RoleDirective implements SchemaDirectiveWiring {

    private final ApplicationAuthorizer authorizer;

    @Inject
    public RoleDirective(final ApplicationAuthorizer authorizer) {
        this.authorizer = authorizer;
    }

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
        final String targetAuthRole = (String) environment.getDirective().getArgument("role").getValue();
        final GraphQLFieldDefinition field = environment.getElement();
        final GraphQLFieldsContainer parentType = environment.getFieldsContainer();
        final DataFetcher<?> originalDataFetcher = environment.getCodeRegistry().getDataFetcher(parentType, field);
        final DataFetcher<?> dataFetcher = dataFetchingEnvironment -> {
            final AuthenticationContext context = dataFetchingEnvironment.getContext();
            final AccountToken token = context.getAccountToken();
            if (authorizer.authorize(token, targetAuthRole)) {
                return originalDataFetcher.get(dataFetchingEnvironment);
            } else {
                throw new UnauthorisedException("You are not authorised to access this resource");
            }
        };

        environment.getCodeRegistry().dataFetcher(parentType, field, dataFetcher);
        return field;
    }
}

