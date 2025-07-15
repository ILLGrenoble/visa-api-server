package eu.ill.visa.security.authentication;


import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.credential.PasswordCredential;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class HybridAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final Logger logger = LoggerFactory.getLogger(HybridAuthenticationMechanism.class);

    private static final String BEARER = "Bearer";
    private static final String BEARER_PREFIX = "bearer ";
    private static final String BASIC_PREFIX = "basic ";
//    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String LOWERCASE_BEARER_PREFIX;
    private static final String LOWERCASE_BASIC_PREFIX;
    private static final int BEARER_PREFIX_LENGTH;
    private static final int BASIC_PREFIX_LENGTH;

    static {
        LOWERCASE_BEARER_PREFIX = BEARER_PREFIX.toLowerCase(Locale.ENGLISH);
        BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
        LOWERCASE_BASIC_PREFIX = BASIC_PREFIX.toLowerCase(Locale.ENGLISH);
        BASIC_PREFIX_LENGTH = BASIC_PREFIX.length();
    }

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
                                              IdentityProviderManager identityProviderManager) {

        final HttpServerRequest request = context.request();
        logger.debug("Obtaining credentials from request with URI {}", request.uri());

        AuthenticationRequest authRequest = fromHeader(request)
//            .or(() -> this.fromCookie(request))
            .orElse(null);

        if (authRequest == null) {
            return Uni.createFrom().nullItem();
        }

        return identityProviderManager.authenticate(authRequest);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        // Just return the Bearer challenge (ignore Basic as it's edge case usage and we don't want browsers to propose a basic auth dialog)
        ChallengeData result = new ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(), HttpHeaderNames.WWW_AUTHENTICATE, BEARER);
        return Uni.createFrom().item(result);
    }

    private Optional<AuthenticationRequest> fromHeader(final HttpServerRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            return Optional.empty();
        }

        if (authHeader.toLowerCase(Locale.ENGLISH).startsWith(LOWERCASE_BEARER_PREFIX)) {
            logger.debug("[AccountToken] Obtained account token from request Authorization Bearer header for URI {}", request.uri());
            TokenCredential tokenCredential = new TokenCredential(authHeader.substring(BEARER_PREFIX_LENGTH), BEARER);
            return Optional.of(new AccountTokenAuthenticationRequest(tokenCredential));

        } else if (authHeader.toLowerCase(Locale.ENGLISH).startsWith(LOWERCASE_BASIC_PREFIX)) {
            try {
                String base64 = authHeader.substring(BASIC_PREFIX_LENGTH).trim();
                String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
                int idx = decoded.indexOf(':');
                if (idx > 0) {
                    logger.debug("[AccountToken] Obtained credentials from request Authorization Basic header for URI {}", request.uri());
                    String username = decoded.substring(0, idx);
                    String password = decoded.substring(idx + 1);
                    return Optional.of(new UsernamePasswordAuthenticationRequest(username, new PasswordCredential(password.toCharArray())));
                }

            } catch (IllegalArgumentException ignored) {
                // Bad base64
            }
            logger.debug("[AccountToken] Failed to obtain credentials from request Authorization Basic header for URI {}", request.uri());
            return Optional.empty();
        }
        return Optional.empty();
    }

//    private Optional<AuthenticationRequest> fromCookie(final HttpServerRequest request) {
//        final Cookie cookie = request.getCookie(ACCESS_TOKEN_COOKIE);
//        if (cookie != null) {
//            logger.debug("[AccountToken] Obtained account token from request access_token cookie for URI {}", request.uri());
//            TokenCredential tokenCredential = new TokenCredential(cookie.getValue(), ACCESS_TOKEN_COOKIE);
//            return Optional.of(new AccountTokenAuthenticationRequest(tokenCredential));
//        }
//        return Optional.empty();
//    }
}
