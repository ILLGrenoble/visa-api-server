package eu.ill.visa.web.healthchecks;

import ru.vyarus.dropwizard.guice.module.installer.feature.health.NamedHealthCheck;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;

public class ApplicationHealthcheck extends NamedHealthCheck {

    @Override
    protected Result check() throws Exception {
        return healthy();
    }

    @Override
    public String getName() {
        return "application healthcheck";
    }
}
