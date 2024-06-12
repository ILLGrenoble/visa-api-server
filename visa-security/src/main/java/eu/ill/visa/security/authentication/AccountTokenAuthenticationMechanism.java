package eu.ill.visa.security.authentication;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class AccountTokenAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final Logger logger = LoggerFactory.getLogger(AccountTokenAuthenticationMechanism.class);

    private static final String BEARER = "Bearer";
    private static final String BEARER_PREFIX = "bearer ";
    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String LOWERCASE_BASIC_PREFIX;
    private static final int PREFIX_LENGTH;

    static {
        LOWERCASE_BASIC_PREFIX = BEARER_PREFIX.toLowerCase(Locale.ENGLISH);
        PREFIX_LENGTH = BEARER_PREFIX.length();
    }

    public AccountTokenAuthenticationMechanism() {
    }

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        final HttpServerRequest request = context.request();

        // Try to get token from header or cookie
        TokenCredential token = this.getTokenFromCookieOrHeader(request);
        if (token != null) {
            AccountTokenAuthenticationRequest credential = new AccountTokenAuthenticationRequest(token);
            HttpSecurityUtils.setRoutingContextAttribute(credential, context);
            context.put(AccountTokenAuthenticationMechanism.class.getName(), this);
            return identityProviderManager.authenticate(credential);
        }

        return Uni.createFrom().optional(Optional.empty());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        ChallengeData result = new ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(), HttpHeaderNames.WWW_AUTHENTICATE, BEARER);
        return Uni.createFrom().item(result);
    }

    private TokenCredential getTokenFromCookieOrHeader(final HttpServerRequest request) {
        final TokenCredential headerToken = getTokenFromRequestHeaders(request);
        if (headerToken == null) {
            return getTokenFromRequestCookies(request);
        }
        return headerToken;
    }

    private TokenCredential getTokenFromRequestHeaders(final HttpServerRequest request) {
        String authHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (authHeader != null) {
            if (authHeader.toLowerCase(Locale.ENGLISH).startsWith(LOWERCASE_BASIC_PREFIX)) {
                logger.debug("[AccountToken] Obtained account token from request Authorization header for URI {}", request.uri());
                return new TokenCredential(authHeader.substring(PREFIX_LENGTH), BEARER);
            }
        }
        return null;
    }

    private TokenCredential getTokenFromRequestCookies(final HttpServerRequest request) {
        final Cookie cookie = request.getCookie(ACCESS_TOKEN_COOKIE);
        if (cookie != null) {
            logger.debug("[AccountToken] Obtained account token from request access_token cookie for URI {}", request.uri());

            return new TokenCredential(cookie.getValue(), ACCESS_TOKEN_COOKIE);
        }
        return null;
    }


}
