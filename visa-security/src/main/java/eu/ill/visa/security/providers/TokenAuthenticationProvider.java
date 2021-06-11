package eu.ill.visa.security.providers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.security.authenticators.TokenAuthenticator;
import eu.ill.visa.security.authorizers.ApplicationAuthorizer;
import eu.ill.visa.security.configuration.TokenConfiguration;
import eu.ill.visa.security.filters.TokenAuthenticationFilter;
import eu.ill.visa.security.tokens.AccountToken;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.ext.Provider;

@Singleton
@Provider
public class TokenAuthenticationProvider extends AuthDynamicFeature {

    @Inject
    TokenAuthenticationProvider(final TokenAuthenticator tokenAuthenticator,
                                final Environment environment,
                                final ApplicationAuthorizer authorizer,
                                final TokenConfiguration configuration
    ) {
        super(createAuthenticationFilter(tokenAuthenticator,
            authorizer,
            configuration));

        environment.jersey().register(RolesAllowedDynamicFeature.class);
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(AccountToken.class));
    }

    private static TokenAuthenticationFilter<AccountToken> createAuthenticationFilter(final TokenAuthenticator tokenAuthenticator,
                                                                                      final ApplicationAuthorizer authorizer,
                                                                                      final TokenConfiguration configuration) {

        return new TokenAuthenticationFilter.Builder<AccountToken>()
            .setPrefix(configuration.getPrefix())
            .setAuthorizer(authorizer)
            .setAuthenticator(tokenAuthenticator)
            .buildAuthFilter();
    }

}
