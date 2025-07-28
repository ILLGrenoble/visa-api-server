package eu.ill.visa.web.rest.filters;

import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.security.tokens.ApplicationToken;
import eu.ill.visa.security.tokens.InstanceToken;
import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Provider
public class LoggingRequestFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestFilter.class);
    private static SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");


    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Context
    SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext context) {
        final String principal = this.getPrincipal(securityContext);

        final String method = context.getMethod();
        final String path = info.getPath();
        final String address = request.remoteAddress().toString();
        final String forwardedIP = request.getHeader("X-Forwarded-For");

        logger.info("{} - {} [{}] \"{} {}\"", forwardedIP == null ? address : forwardedIP, principal, format.format(new Date()), method, path);
    }

    private String getPrincipal(SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal instanceof AccountToken accountToken) {
            return principal.getName() + " (" + accountToken.getUser().getId() + ")";

        } else if (principal instanceof InstanceToken instanceToken) {
            return "Instance " + instanceToken.getInstance().getId();

        } else if (principal instanceof ApplicationToken applicationToken) {
            return "Application credential " + applicationToken.getName();
        }
        return "-";
    }
}
