package eu.ill.visa.web.bundles.swagger;

import com.google.common.base.Charsets;
import io.dropwizard.views.View;

public class SwaggerView extends View {

    private final String swaggerAssetsPath;
    private final String contextPath;

    protected SwaggerView(String urlPattern) {
        super("index.ftl", Charsets.UTF_8);
        final String delimiter = "/";
        if (urlPattern.equals(delimiter)) {
            swaggerAssetsPath = Constants.SWAGGER_URI_PATH;
        } else {
            swaggerAssetsPath = urlPattern + Constants.SWAGGER_URI_PATH;
        }

        if (urlPattern.equals(delimiter)) {
            contextPath = "";
        } else {
            contextPath = urlPattern;
        }
    }

    /**
     * Returns the path with which all requests for Swagger's static content need to be prefixed
     */
    @SuppressWarnings("unused")
    public String getSwaggerAssetsPath() {
        return swaggerAssetsPath;
    }

    /**
     * Returns the path with with which all requests made by Swagger's UI to Resources need to be prefixed
     */
    @SuppressWarnings("unused")
    public String getContextPath() {
        return contextPath;
    }
}
