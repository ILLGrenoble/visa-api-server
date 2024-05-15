package eu.ill.visa.web.graphql.context;

import eu.ill.visa.security.authenticator.AccountTokenAuthenticator;
import eu.ill.visa.security.tokens.AccountToken;
import graphql.kickstart.execution.context.GraphQLKickstartContext;
import graphql.kickstart.servlet.context.DefaultGraphQLServletContextBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.Session;
import jakarta.websocket.server.HandshakeRequest;
import org.dataloader.DataLoaderRegistry;

import java.util.Optional;

@ApplicationScoped
public class AuthenticationContextBuilder extends DefaultGraphQLServletContextBuilder {

    private final AccountTokenAuthenticator authenticator;

    @Inject
    public AuthenticationContextBuilder(final AccountTokenAuthenticator authenticator) {
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
                return new AuthenticationContext(buildDataLoaderRegistry(), accountToken.get());
            }
        }
        return new AuthenticationContext(buildDataLoaderRegistry(), null);
    }

    @Override
    public GraphQLKickstartContext build(HttpServletRequest request, HttpServletResponse response) {
        return getAuthenticationContext(request.getHeader("Authorization"));
    }

    @Override
    public GraphQLKickstartContext build(Session session, HandshakeRequest handshakeRequest) {
        return new AuthenticationContext(buildDataLoaderRegistry(), null);
    }

    private DataLoaderRegistry buildDataLoaderRegistry() {
        return new DataLoaderRegistry();
    }
}
