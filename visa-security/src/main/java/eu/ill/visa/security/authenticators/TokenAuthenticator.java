package eu.ill.visa.security.authenticators;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.security.configuration.TokenConfiguration;
import eu.ill.visa.security.tokens.AccountToken;
import io.dropwizard.auth.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static java.util.Objects.requireNonNullElse;

@Singleton
public class TokenAuthenticator implements Authenticator<String, AccountToken> {
    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticator.class);
    private final UserService userService;
    private final OkHttpClient client;
    private final String url;

    @Inject
    public TokenAuthenticator(UserService userService, final TokenConfiguration configuration) {
        this.client = new OkHttpClient();
        this.userService = userService;
        this.url = configuration.getAccountsUrl();
    }

    private User createUserFromData(final String id,
                                      final String firstName,
                                      final String lastName,
                                      final String email) {
        final User.Builder builder = new User.Builder();

        return builder
            .id(id)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .activatedAt(new Date())
            .lastSeenAt(new Date())
            .build();
    }

    private User getOrCreateUser(User user) {
        final String id = user.getId();
        final String firstName = user.getFirstName();
        final String lastName = user.getLastName();
        final String email = user.getEmail();

        if ("0".equals(id)) {
            return createUserFromData("0", firstName, lastName, email);
        }

        // Generate a persisted version of the User object
        user = requireNonNullElse(userService.getById(id), createUserFromData(id, firstName, lastName, email));

        // Set activated date if not already
        if (user.getActivatedAt() == null) {
            user.setActivatedAt(new Date());
        }

        user.setLastSeenAt(new Date());
        userService.save(user);
        return user;
    }

    private AccountToken parseJson(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(response, AccountToken.class);
    }

    private AccountToken getAccountToken(final String token) {
        final Request request = new Builder()
            .url(this.url)
            .addHeader("x-access-token", token)
            .build();

        try (final Response response = this.client.newCall(request).execute()) {

            if (response.body() == null) {
                return null;
            }

            String responseBody = response.body().string();
            if (response.code() == 200) {
                AccountToken account = parseJson(responseBody);

                return account;

            } else if (response.code() == 401) {
                logger.info("[Token] Caught unauthenticated access to VISA: {}", responseBody);

            } else {
                logger.error("[Token] Caught HTTP error ({}: {}) authenticating user access token", response.code(), response.message());
            }

        } catch (IOException e) {
            logger.error("[Token] Error obtaining account from access token: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public Optional<AccountToken> authenticate(final String token) {
        logger.debug("[Token] Authenticating token");

        final AccountToken accountToken = this.getAccountToken(token);

        if (accountToken != null) {
            if (accountToken.getUser() != null) {
                // Convert basic JSON user into a persisted one
                final User user = getOrCreateUser(accountToken.getUser());
                accountToken.setUser(user);

                if ("0".equals(user.getId())) {
                    logger.warn("[Token] User {} with login {} has an invalid user id (0)", user.getFullName(), accountToken.getName());

                } else {
                    logger.info("[Token] Successfully authenticated user: {} ({})", accountToken.getName(), user.getId());
                }

                return Optional.of(accountToken);

            } else {
                logger.error("[Token] Did not obtain a valid user from the Account Service for username {}", accountToken.getName());
            }
        }

        return Optional.empty();
    }
}
