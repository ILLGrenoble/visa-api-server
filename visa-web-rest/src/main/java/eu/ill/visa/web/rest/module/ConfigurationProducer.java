package eu.ill.visa.web.rest.module;

import eu.ill.visa.web.rest.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class ConfigurationProducer {

    private final ClientConfiguration clientConfiguration;

    @Inject
    public ConfigurationProducer(final ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    @Produces
    public LoginConfiguration loginConfiguration() {
        return this.clientConfiguration.loginConfiguration();
    }

    @Produces
    public AnalyticsConfiguration analyticsConfiguration() {
        return this.clientConfiguration.analyticsConfiguration();
    }

    @Produces
    public DesktopConfiguration desktopConfiguration() {
        return this.clientConfiguration.desktopConfiguration();
    }

    @Produces
    public ExperimentsConfiguration experimentsConfiguration() {
        return this.clientConfiguration.experimentsConfiguration();
    }

}
