package eu.ill.visa.web.bundles.swagger;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static eu.ill.visa.web.bundles.swagger.Constants.SWAGGER_ASSETS_NAME;
import static eu.ill.visa.web.bundles.swagger.Constants.SWAGGER_RESOURCES_PATH;

public abstract class SwaggerBundle<T extends Configuration> implements ConfiguredBundle<T> {

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addBundle(new ViewBundle<Configuration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(final Configuration configuration) {
                return ImmutableMap.of();
            }
        });
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        SwaggerBundleConfiguration swaggerBundleConfiguration = getSwaggerBundleConfiguration(configuration);
        if (swaggerBundleConfiguration == null) {
            throw new IllegalStateException("You need to provide an instance of SwaggerBundleConfiguration");
        }

        ConfigurationHelper configurationHelper = new ConfigurationHelper(configuration, swaggerBundleConfiguration);
        new AssetsBundle(SWAGGER_RESOURCES_PATH, configurationHelper.getSwaggerUriPath(), null, SWAGGER_ASSETS_NAME).run(environment);

        environment.jersey().register(new SwaggerResource(configurationHelper.getUrlPattern()));
        environment.getObjectMapper().setSerializationInclusion(NON_NULL);

        setUpSwagger(swaggerBundleConfiguration, configurationHelper.getUrlPattern());
        environment.jersey().register(new ApiListingResource());
    }

    @SuppressWarnings("unused")
    protected abstract SwaggerBundleConfiguration getSwaggerBundleConfiguration(T configuration);

    private void setUpSwagger(SwaggerBundleConfiguration swaggerBundleConfiguration, String urlPattern) {
        BeanConfig config = new BeanConfig();

        if (swaggerBundleConfiguration.getTitle() != null) {
            config.setTitle(swaggerBundleConfiguration.getTitle());
        }

        if (swaggerBundleConfiguration.getVersion() != null) {
            config.setVersion(swaggerBundleConfiguration.getVersion());
        }

        if (swaggerBundleConfiguration.getDescription() != null) {
            config.setDescription(swaggerBundleConfiguration.getDescription());
        }

        if (swaggerBundleConfiguration.getContact() != null) {
            config.setContact(swaggerBundleConfiguration.getContact());
        }

        if (swaggerBundleConfiguration.getLicense() != null) {
            config.setLicense(swaggerBundleConfiguration.getLicense());
        }

        if (swaggerBundleConfiguration.getLicenseUrl() != null) {
            config.setLicenseUrl(swaggerBundleConfiguration.getLicenseUrl());
        }

        if (swaggerBundleConfiguration.getTermsOfServiceUrl() != null) {
            config.setTermsOfServiceUrl(swaggerBundleConfiguration.getTermsOfServiceUrl());
        }

        config.setBasePath(urlPattern);

        if (swaggerBundleConfiguration.getResourcePackage() != null) {
            config.setResourcePackage(swaggerBundleConfiguration.getResourcePackage());
        } else {
            throw new IllegalStateException("Resource package needs to be specified for Swagger to correctly detect annotated resources");
        }

        config.setScan(true);
    }
}
