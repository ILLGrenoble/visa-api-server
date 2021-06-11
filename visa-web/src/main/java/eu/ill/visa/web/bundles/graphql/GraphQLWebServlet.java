package eu.ill.visa.web.bundles.graphql;

import com.google.inject.Inject;
import eu.ill.visa.web.bundles.graphql.context.AuthenticationContextBuilder;
import eu.ill.visa.web.bundles.graphql.directives.RoleDirective;
import eu.ill.visa.web.bundles.graphql.instrumentation.AuthenticationInstrumentation;
import eu.ill.visa.web.bundles.graphql.queries.resolvers.MutationResolver;
import eu.ill.visa.web.bundles.graphql.queries.resolvers.QueryResolver;
import eu.ill.visa.web.bundles.graphql.queries.resolvers.fields.*;
import eu.ill.visa.web.bundles.graphql.scalars.DateScalar;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.kickstart.execution.GraphQLQueryInvoker;
import graphql.kickstart.tools.SchemaParser;
import graphql.kickstart.tools.SchemaParserOptions;
import graphql.schema.GraphQLSchema;
import graphql.servlet.GraphQLConfiguration;
import graphql.servlet.GraphQLHttpServlet;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;


@WebServlet(urlPatterns = "/graphql/*", loadOnStartup = 0, name = "graphql", asyncSupported = true)
public class GraphQLWebServlet extends GraphQLHttpServlet {

    private final GraphQLWebServletConfiguration configuration;
    private final QueryResolver queryResolver;
    private final MutationResolver mutationResolver;
    private final InstanceResolver instanceResolver;
    private final InstanceSessionMemberResolver instanceSessionMemberResolver;
    private final ImageResolver imageResolver;
    private final CloudImageResolver cloudImageResolver;
    private final ImageProtocolResolver imageProtocolResolver;
    private final UserResolver userResolver;
    private final RoleDirective roleDirective;
    private final AuthenticationContextBuilder authenticationContext;

    @Inject
    public GraphQLWebServlet(final GraphQLWebServletConfiguration configuration,
                             final QueryResolver queryResolver,
                             final MutationResolver mutationResolver,
                             final InstanceResolver instanceResolver,
                             final InstanceSessionMemberResolver instanceSessionMemberResolver,
                             final UserResolver userResolver,
                             final ImageResolver imageResolver,
                             final CloudImageResolver cloudImageResolver,
                             final ImageProtocolResolver imageProtocolResolver,
                             final AuthenticationContextBuilder authenticationContext,
                             final RoleDirective roleDirective) {
        this.configuration = configuration;
        this.queryResolver = queryResolver;
        this.mutationResolver = mutationResolver;
        this.instanceResolver = instanceResolver;
        this.instanceSessionMemberResolver = instanceSessionMemberResolver;
        this.userResolver = userResolver;
        this.imageResolver = imageResolver;
        this.cloudImageResolver = cloudImageResolver;
        this.imageProtocolResolver = imageProtocolResolver;
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
        if (configuration.getTracing()) {
            chainedList.add(new TracingInstrumentation());
        }

        builder.with(chainedList);
        return builder.build();
    }

    private String[] getSchemaFiles() {
        final List<String> files = configuration.getFiles();
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
                userResolver,
                imageResolver,
                cloudImageResolver,
                imageProtocolResolver,
                userResolver
            )
            .directive("isAuthorised", roleDirective)

            .scalars(new DateScalar())
            .build()
            .makeExecutableSchema();
    }


}


