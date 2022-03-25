package eu.ill.visa.security.providers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

import javax.ws.rs.ext.Provider;
import java.security.Principal;

@Singleton
@Provider
public class AuthenticationProvider extends PolymorphicAuthDynamicFeature<Principal> {

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

        environment.jersey().register(binder);

        environment.jersey().register(RolesAllowedDynamicFeature.class);
    }

    private static TokenAuthenticationFilter<AccountToken> createAccountAuthenticationFilter(final TokenAuthenticator tokenAuthenticator,
                                                                                             final ApplicationAuthorizer authorizer,
                                                                                             final TokenConfiguration configuration) {

        return new TokenAuthenticationFilter.Builder<AccountToken>()
            .setPrefix(configuration.getPrefix())
            .setAuthorizer(authorizer)
            .setAuthenticator(tokenAuthenticator)
            .buildAuthFilter();
    }

    private static BasicCredentialAuthFilter<ApplicationToken> createBasicAuthenticationFilter(final ApplicationCredentialAuthenticator applicationCredentialAuthenticator) {

        return new BasicCredentialAuthFilter.Builder<ApplicationToken>()
            .setPrefix("Basic")
            .setAuthenticator(applicationCredentialAuthenticator)
            .buildAuthFilter();
    }
}
