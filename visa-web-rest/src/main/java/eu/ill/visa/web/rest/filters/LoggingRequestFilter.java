package eu.ill.visa.web.rest.filters;

import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class LoggingRequestFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingRequestFilter.class);

    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext context) {

        final String method = context.getMethod();
        final String path = info.getPath();
        final String address = request.remoteAddress().toString();
        final String forwardedIP = request.getHeader("X-Forwarded-For");

        logger.info("{} {} from IP {}", method, path, forwardedIP == null ? address : forwardedIP);
    }
}
