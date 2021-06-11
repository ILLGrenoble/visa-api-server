package eu.ill.visa.web.bundles.swagger;

public class Constants {
    /**
     * The project's directory in which Swagger static assets live
     */
    public static final String SWAGGER_RESOURCES_PATH = "/swagger-static";

    /**
     * The path with which all HTTP requests for Swagger assets should be prefixed.
     */
    public static final String SWAGGER_URI_PATH = SWAGGER_RESOURCES_PATH;

    /**
     * The name of the {@link io.dropwizard.assets.AssetsBundle} to register.
     */
    public static final String SWAGGER_ASSETS_NAME = "swagger-assets";

    /**
     * The path to which Swagger resources are bound to
     */
    public static final String SWAGGER_PATH = "/api-docs";
}
