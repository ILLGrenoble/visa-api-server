package eu.ill.visa.web.rest;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Optional;

@ConfigMapping(prefix = "client", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface ClientConfiguration {

    @WithName("login")
    LoginConfiguration loginConfiguration();

    @WithName("analytics")
    AnalyticsConfiguration analyticsConfiguration();

    @WithName("desktop")
    DesktopConfiguration desktopConfiguration();

    @WithName("experiments")
    ExperimentsConfiguration experimentsConfiguration();

    Optional<String> contactEmail();

}
