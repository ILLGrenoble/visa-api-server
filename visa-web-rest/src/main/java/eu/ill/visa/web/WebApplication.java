package eu.ill.visa.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import eu.ill.visa.business.BusinessModule;
import eu.ill.visa.cloud.CloudModule;
import eu.ill.visa.scheduler.SchedulerModule;
import eu.ill.visa.security.SecurityModule;
import eu.ill.visa.vdi.VirtualDesktopModule;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.bval.guice.ValidationModule;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import java.util.EnumSet;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public class WebApplication extends Application<WebConfiguration> {

    private static final String BASE_PACKAGES = "eu.ill.visa";

    @Override
    public String getName() {
        return "VISA API";
    }

    @Override
    public void initialize(Bootstrap<WebConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false))
        );
        addBundles(bootstrap);
    }

    private void addBundles(Bootstrap<WebConfiguration> bootstrap) {
        GuiceBundle bundle = configureGuiceBundle();
        bootstrap.addBundle(bundle);
        bootstrap.addBundle(new MultiPartBundle());
    }

    private GuiceBundle configureGuiceBundle() {
        return GuiceBundle.<WebConfiguration>builder()
            .enableAutoConfig(BASE_PACKAGES)
            .modules(new CloudModule())
            .modules(new SchedulerModule())
            .modules(new SecurityModule())
            .modules(new VirtualDesktopModule())
            .modules(new WebModule())
            .modules(new BusinessModule())
            .modules(new ValidationModule())
            .build();
    }

    private void registerCors(final WebConfiguration configuration, final Environment environment) {
        final Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        cors.setInitParameter("allowedOrigins", configuration.getCorsOrigin());
        cors.setInitParameter("allowedMethods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
        cors.setInitParameter("allowedHeaders", "Authorization, Content-Type");

        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    private void registerObjectMapping(final Environment environment) {
        final ObjectMapper objectMapper = environment.getObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.setSerializationInclusion(ALWAYS);
        objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.enable(STRICT_DUPLICATE_DETECTION);
    }

    @Override
    public void run(final WebConfiguration configuration, final Environment environment) {
        registerCors(configuration, environment);
        registerObjectMapping(environment);
    }

}
