package eu.ill.visa.web.bundles.swagger;


import io.dropwizard.Configuration;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;

import java.util.Optional;

/**
 * Wrapper around Dropwizard's configuration and the bundle's config that simplifies getting some
 * information from them.
 */
public class ConfigurationHelper {

    private final Configuration              configuration;
    private final SwaggerBundleConfiguration swaggerBundleConfiguration;

    public ConfigurationHelper(Configuration configuration, SwaggerBundleConfiguration swaggerBundleConfiguration) {
        this.configuration = configuration;
        this.swaggerBundleConfiguration = swaggerBundleConfiguration;
    }

    public String getJerseyRootPath() {
        // if the user explictly defined a path to prefix requests use it instead of derive it
        if (swaggerBundleConfiguration.getUriPrefix() != null) {
            return swaggerBundleConfiguration.getUriPrefix();
        }

        Optional<String> rootPath;

        ServerFactory serverFactory = configuration.getServerFactory();

        if (serverFactory instanceof SimpleServerFactory) {
            rootPath = ((SimpleServerFactory) serverFactory).getJerseyRootPath();
        } else {
            rootPath = ((DefaultServerFactory) serverFactory).getJerseyRootPath();
        }

        return stripUrlSlashes(rootPath.get());
    }

    public String getUrlPattern() {
        // if the user explictly defined a path to prefix requests use it instead of derive it
        if (swaggerBundleConfiguration.getUriPrefix() != null) {
            return swaggerBundleConfiguration.getUriPrefix();
        }

        final String applicationContextPath = getApplicationContextPath();
        final String rootPath               = getJerseyRootPath();

        String       urlPattern;
        final String delimiter = "/";
        if (rootPath.equals(delimiter) && applicationContextPath.equals(delimiter)) {
            urlPattern = "/";
        } else if (rootPath.equals(delimiter) && !applicationContextPath.equals(delimiter)) {
            urlPattern = applicationContextPath;
        } else if (!rootPath.equals(delimiter) && applicationContextPath.equals(delimiter)) {
            urlPattern = rootPath;
        } else {
            urlPattern = applicationContextPath + rootPath;
        }

        return urlPattern;
    }

    public String getSwaggerUriPath() {
        final String delimiter      = "/";
        final String jerseyRootPath = getJerseyRootPath();
        String       uriPathPrefix  = jerseyRootPath.equals(delimiter) ? "" : jerseyRootPath;
        return uriPathPrefix + Constants.SWAGGER_URI_PATH;
    }

    private String getApplicationContextPath() {
        String applicationContextPath;

        ServerFactory serverFactory = configuration.getServerFactory();

        if (serverFactory instanceof SimpleServerFactory) {
            applicationContextPath = ((SimpleServerFactory) serverFactory).getApplicationContextPath();
        } else {
            applicationContextPath = ((DefaultServerFactory) serverFactory).getApplicationContextPath();
        }

        return stripUrlSlashes(applicationContextPath);
    }

    private String stripUrlSlashes(String urlToStrip) {
        if (urlToStrip.endsWith("/*")) {
            urlToStrip = urlToStrip.substring(0, urlToStrip.length() - 1);
        }

        if (urlToStrip.length() > 1 && urlToStrip.endsWith("/")) {
            urlToStrip = urlToStrip.substring(0, urlToStrip.length() - 1);
        }

        return urlToStrip;
    }
}
