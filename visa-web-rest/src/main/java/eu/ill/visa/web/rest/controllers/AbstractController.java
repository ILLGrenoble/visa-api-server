package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.core.entity.User;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.UnauthorizedException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;
import org.jboss.resteasy.reactive.RestResponse.Status;

import java.security.Principal;
import java.util.List;

public abstract class AbstractController {

    protected <T> MetaResponse<T> createResponse() {
        return createResponse(null);
    }

    protected <T> MetaResponse<T> createResponse(final T entity) {
        return createResponse(entity, null, null);
    }

    protected <T> MetaResponse<T> createResponse(final T entity, final MetaResponse.MetaData metadata) {
        return createResponse(entity, metadata, null);
    }

    protected <T> MetaResponse<T> createResponse(final T entity, final MetaResponse.MetaData metadata, final List<String> errors) {
        return new MetaResponse<>(entity, metadata, errors);
    }

    protected <T> RestResponse<MetaResponse<T>> createResponse(final T entity, final Status status) {
        return ResponseBuilder.create(status, new MetaResponse<>(entity, null, null)).build();
    }

    protected void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new BadRequestException(message);
        }
    }

    protected User getUserPrincipal(SecurityContext securityContext) {
        return this.getAccountToken(securityContext).getUser();
    }

    protected AccountToken getAccountToken(SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        if (principal instanceof AccountToken) {
            return ((AccountToken) principal);
        }
        throw new UnauthorizedException("SecurityContext does not hold a valid AccountToken");
    }


}
