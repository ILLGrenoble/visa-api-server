package eu.ill.visa.security.providers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import eu.ill.visa.security.authenticators.ApplicationCredentialAuthenticator;
import eu.ill.visa.security.authenticators.TokenAuthenticator;
import eu.ill.visa.security.authorizers.ApplicationAuthorizer;
import eu.ill.visa.security.configuration.TokenConfiguration;
import eu.ill.visa.security.filters.TokenAuthenticationFilter;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.security.tokens.ApplicationToken;
import io.dropwizard.auth.PolymorphicAuthDynamicFeature;
import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.security.Principal;

@Singleton
@Provider
public class AuthenticationProvider extends PolymorphicAuthDynamicFeature<Principal> {

    private static TokenAuthenticationFilter<AccountToken> tokenTokenAuthenticationFilter;

    @Inject
    AuthenticationProvider(final TokenAuthenticator tokenAuthenticator,
                           final ApplicationCredentialAuthenticator applicationCredentialAuthenticator,
                           final Environment environment,
                           final ApplicationAuthorizer authorizer,
                           final TokenConfiguration configuration) {
        super(ImmutableMap.of(
            AccountToken.class, createAccountAuthenticationFilter(tokenAuthenticator, authorizer, configuration),
            ApplicationToken.class, createBasicAuthenticationFilter(applicationCredentialAuthenticator)
        ));

        final AbstractBinder binder = new PolymorphicAuthValueFactoryProvider.Binder<>(
            ImmutableSet.of(
                AccountToken.class,
                ApplicationToken.class
            )
        );

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(binder);
    }

    private synchronized static TokenAuthenticationFilter<AccountToken> createAccountAuthenticationFilter(final TokenAuthenticator tokenAuthenticator,
                                                                                             final ApplicationAuthorizer authorizer,
                                                                                             final TokenConfiguration configuration) {

        if (AuthenticationProvider.tokenTokenAuthenticationFilter == null) {
            AuthenticationProvider.tokenTokenAuthenticationFilter = new TokenAuthenticationFilter.Builder<AccountToken>()
                .setPrefix(configuration.getPrefix())
                .setAuthorizer(authorizer)
                .setAuthenticator(tokenAuthenticator)
                .buildAuthFilter();
        }

        return AuthenticationProvider.tokenTokenAuthenticationFilter;
    }

    private static BasicCredentialAuthFilter<ApplicationToken> createBasicAuthenticationFilter(final ApplicationCredentialAuthenticator applicationCredentialAuthenticator) {

        return new BasicCredentialAuthFilter.Builder<ApplicationToken>()
            .setAuthenticator(applicationCredentialAuthenticator)
            .buildAuthFilter();
    }

    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        // Do configuration checking for @Auth parameters
        int instanceCountBefore =  context.getConfiguration().getInstances().size();
        super.configure(resourceInfo, context);
        int instanceCountAfter =  context.getConfiguration().getInstances().size();

        // If the context hasn't been modified then check for PermitAll on the class annotation
        if (instanceCountAfter == instanceCountBefore) {
            final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
            boolean annotationOnClass = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class) != null || resourceInfo.getResourceClass().getAnnotation(PermitAll.class) != null;
            boolean annotationOnMethod = am.isAnnotationPresent(RolesAllowed.class) || am.isAnnotationPresent(DenyAll.class) || am.isAnnotationPresent(PermitAll.class);
            if (annotationOnClass || annotationOnMethod) {
                context.register(AuthenticationProvider.tokenTokenAuthenticationFilter);
            }
        }
    }
}
