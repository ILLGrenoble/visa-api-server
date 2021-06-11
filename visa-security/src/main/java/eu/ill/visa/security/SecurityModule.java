package eu.ill.visa.security;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.ill.visa.security.configuration.TokenConfiguration;

public class SecurityModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    public TokenConfiguration providesTokenAuthenticationConfiguration(
        final SecurityConfiguration configuration) {
        return configuration.getTokenConfiguration();
    }

}
