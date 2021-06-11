package eu.ill.visa.security.filters;

import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

@Priority(Priorities.AUTHENTICATION)
public class TokenAuthenticationFilter<P extends Principal> extends AuthFilter<String, P> {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    private TokenAuthenticationFilter() {
    }

    private void setRequestContext(final ContainerRequestContext requestContext, P principal) {
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(final String role) {
                return authorizer.authorize(principal, role);
            }

            @Override
            public boolean isSecure() {
                return requestContext.getSecurityContext().isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return BASIC_AUTH;
            }
        });
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        final String token = getTokenFromCookieOrHeader(requestContext);

        try {
            if (token != null) {
                final Optional<P> principal = authenticator.authenticate(token);

                if (principal.isPresent()) {
                    setRequestContext(requestContext, principal.get());
                    return;
                }
            }
        } catch (AuthenticationException exception) {
            logger.error("[Token] Error authenticating credentials", exception.getMessage());
            throw new InternalServerErrorException();
        }
        throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }

    private String getTokenFromHeader(final MultivaluedMap<String, String> headers) {
        final String header = headers.getFirst(AUTHORIZATION);
        if (header != null) {
            int space = header.indexOf(' ');
            if (space > 0) {
                final String method = header.substring(0, space);
                if (prefix.equalsIgnoreCase(method)) {
                    return header.substring(space + 1);
                }
            }
        }
        return null;
    }

    private String getTokenFromCookieOrHeader(final ContainerRequestContext requestContext) {
        final String headerToken = getTokenFromHeader(requestContext.getHeaders());
        if (headerToken == null) {
            return getTokenFromCookie(requestContext);
        }
        return headerToken;
    }

    private String getTokenFromCookie(final ContainerRequestContext requestContext) {
        final Map<String, Cookie> cookies = requestContext.getCookies();

        if (cookies.containsKey("access_token")) {
            final Cookie tokenCookie = cookies.get("access_token");
            return tokenCookie.getValue();
        }

        return null;
    }


    /**
     * Builder for {@link TokenAuthenticationFilter}.
     * <p>An {@link Authenticator} must be provided during the building process.</p>
     *
     * @param <P> the principal
     */
    public static class Builder<P extends Principal> extends AuthFilterBuilder<String, P, TokenAuthenticationFilter<P>> {

        @Override
        protected TokenAuthenticationFilter<P> newInstance() {
            return new TokenAuthenticationFilter<>();
        }
    }
}
