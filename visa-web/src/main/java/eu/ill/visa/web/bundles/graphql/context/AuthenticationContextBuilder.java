package eu.ill.visa.web.bundles.graphql.context;

import com.google.inject.Inject;
import eu.ill.visa.security.authenticators.TokenAuthenticator;
import eu.ill.visa.security.tokens.AccountToken;
import graphql.kickstart.execution.context.GraphQLContext;
import graphql.servlet.context.DefaultGraphQLServletContextBuilder;
import org.dataloader.DataLoaderRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import java.util.Optional;

public class AuthenticationContextBuilder extends DefaultGraphQLServletContextBuilder {

    private final TokenAuthenticator authenticator;

    @Inject
    public AuthenticationContextBuilder(final TokenAuthenticator authenticator) {
        this.authenticator = authenticator;
    }


    private Optional<String> getTokenFromHeader(final String header) {
        if (header != null) {
            int space = header.indexOf(' ');
            if (space > 0) {
                final String method = header.substring(0, space);
                if ("Bearer".equalsIgnoreCase(method)) {
                    return Optional.of(header.substring(space + 1));
                }
            }
        }
        return Optional.empty();
    }

    public AuthenticationContext getAuthenticationContext(final String header) {
        final Optional<String> token = getTokenFromHeader(header);
        if (token.isPresent()) {
            final Optional<AccountToken> accountToken = authenticator.authenticate(token.get());
            if (accountToken.isPresent()) {
                return new AuthenticationContext(buildDataLoaderRegistry(), null, accountToken.get());
            }
        }
        return new AuthenticationContext(buildDataLoaderRegistry(), null, null);
    }

    @Override
    public GraphQLContext build(HttpServletRequest request, HttpServletResponse response) {
        return getAuthenticationContext(request.getHeader("Authorization"));
    }

    @Override
    public GraphQLContext build(Session session, HandshakeRequest handshakeRequest) {
        return new AuthenticationContext(buildDataLoaderRegistry(), null, null);
    }

    private DataLoaderRegistry buildDataLoaderRegistry() {
        return new DataLoaderRegistry();
    }
}
