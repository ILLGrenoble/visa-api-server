package eu.ill.visa.web.graphql;

import eu.ill.visa.web.graphql.context.AuthenticationContextBuilder;
import eu.ill.visa.web.graphql.directives.RoleDirective;
import eu.ill.visa.web.graphql.instrumentation.AuthenticationInstrumentation;
import eu.ill.visa.web.graphql.queries.resolvers.MutationResolver;
import eu.ill.visa.web.graphql.queries.resolvers.QueryResolver;
import eu.ill.visa.web.graphql.queries.resolvers.fields.*;
import eu.ill.visa.web.graphql.scalars.DateScalar;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.servlet.GraphQLConfiguration;
import graphql.kickstart.servlet.GraphQLHttpServlet;
import graphql.kickstart.tools.SchemaParser;
import graphql.kickstart.tools.SchemaParserOptions;
import graphql.schema.GraphQLSchema;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;

import java.util.ArrayList;
import java.util.List;


@WebServlet(urlPatterns = "/graphql/*", loadOnStartup = 0, name = "graphql", asyncSupported = true)
public class GraphQLWebServlet extends GraphQLHttpServlet {

    private final GraphQLWebServletConfiguration configuration;
    private final QueryResolver                  queryResolver;
    private final MutationResolver               mutationResolver;
    private final InstanceResolver               instanceResolver;
    private final InstanceSessionMemberResolver instanceSessionMemberResolver;
    private final InstanceJupyterSessionResolver instanceJupyterSessionResolver;
    private final ImageResolver                  imageResolver;
    private final FlavourResolver flavourResolver;
    private final CloudImageResolver cloudImageResolver;
    private final CloudClientResolver            cloudClientResolver;
    private final ImageProtocolResolver          imageProtocolResolver;
    private final UserResolver                   userResolver;
    private final RoleDirective                  roleDirective;
    private final AuthenticationContextBuilder authenticationContext;
    private final SecurityGroupFilterResolver securityGroupFilterResolver;
    private final SecurityGroupResolver securityGroupResolver;

    @Inject
    public GraphQLWebServlet(final GraphQLWebServletConfiguration configuration,
                             final QueryResolver queryResolver,
                             final MutationResolver mutationResolver,
                             final InstanceResolver instanceResolver,
                             final InstanceSessionMemberResolver instanceSessionMemberResolver,
                             final InstanceJupyterSessionResolver instanceJupyterSessionResolver,
                             final UserResolver userResolver,
                             final ImageResolver imageResolver,
                             final FlavourResolver flavourResolver,
                             final CloudImageResolver cloudImageResolver,
                             final CloudClientResolver cloudClientResolver,
                             final ImageProtocolResolver imageProtocolResolver,
                             final SecurityGroupFilterResolver securityGroupFilterResolver,
                             final SecurityGroupResolver securityGroupResolver,
                             final AuthenticationContextBuilder authenticationContext,
                             final RoleDirective roleDirective) {
        this.configuration = configuration;
        this.queryResolver = queryResolver;
        this.mutationResolver = mutationResolver;
        this.instanceResolver = instanceResolver;
        this.instanceSessionMemberResolver = instanceSessionMemberResolver;
        this.instanceJupyterSessionResolver = instanceJupyterSessionResolver;
        this.userResolver = userResolver;
        this.imageResolver = imageResolver;
        this.flavourResolver = flavourResolver;
        this.cloudImageResolver = cloudImageResolver;
        this.cloudClientResolver = cloudClientResolver;
        this.imageProtocolResolver = imageProtocolResolver;
        this.securityGroupFilterResolver = securityGroupFilterResolver;
        this.securityGroupResolver = securityGroupResolver;
        this.authenticationContext = authenticationContext;
        this.roleDirective = roleDirective;
    }

    @Override
    protected GraphQLConfiguration getConfiguration() {
        final GraphQLConfiguration.Builder builder = GraphQLConfiguration
            .with(buildSchema())
            .with(authenticationContext)
            .with(buildQueryInvoker());
        return builder.build();
    }


    private GraphQLQueryInvoker buildQueryInvoker() {
        GraphQLQueryInvoker.Builder builder = GraphQLQueryInvoker.newBuilder();
        List<Instrumentation> chainedList = new ArrayList<>();
        chainedList.add(new AuthenticationInstrumentation());
        chainedList.add(new DataLoaderDispatcherInstrumentation());
        if (configuration.tracing()) {
            chainedList.add(new TracingInstrumentation());
        }

        builder.with(chainedList);
        return builder.build();
    }

    private String[] getSchemaFiles() {
        final List<String> files = configuration.files();
        return files.toArray(new String[0]);
    }

    private GraphQLSchema buildSchema() {

        final SchemaParserOptions.Builder builder = SchemaParserOptions.newOptions();
        final SchemaParserOptions options = builder.build();
        return SchemaParser.newParser()
            .options(options)
            .files(getSchemaFiles())
            .resolvers(
                queryResolver,
                mutationResolver,
                instanceResolver,
                instanceSessionMemberResolver,
                instanceJupyterSessionResolver,
                userResolver,
                imageResolver,
                flavourResolver,
                cloudImageResolver,
                cloudClientResolver,
                imageProtocolResolver,
                userResolver,
                securityGroupResolver,
                securityGroupFilterResolver
            )
            .directive("isAuthorised", roleDirective)

            .scalars(DateScalar.DATE)
            .build()
            .makeExecutableSchema();
    }


}


