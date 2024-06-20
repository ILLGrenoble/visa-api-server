package eu.ill.visa.web.rest.filters;

import eu.ill.visa.core.entity.User;
import eu.ill.visa.security.tokens.AccountToken;
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

@Provider
public class LoggingRequestFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestFilter.class);

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

        logger.debug("{} {} {}, user: {}", forwardedIP == null ? address : forwardedIP, method, path, principal);
    }

    private String getPrincipal(SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal instanceof AccountToken) {
            User user = ((AccountToken) principal).getUser();
            return principal.getName() + " (" + user.getId() + ")";
        }
        return "anonymous";
    }
}
