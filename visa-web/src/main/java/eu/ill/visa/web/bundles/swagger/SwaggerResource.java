package eu.ill.visa.web.bundles.swagger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Constants.SWAGGER_PATH)
@Produces(MediaType.TEXT_HTML)
public class SwaggerResource {
    private String urlPattern;

    public SwaggerResource() {

    }

    public SwaggerResource(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    @GET
    public SwaggerView get() {
        return new SwaggerView(urlPattern);
    }
}
