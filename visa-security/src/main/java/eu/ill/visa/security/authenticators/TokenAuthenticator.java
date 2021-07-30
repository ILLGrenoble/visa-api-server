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
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

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

    private AccountToken parseJson(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(response, AccountToken.class);
    }

    @Override
    public Optional<AccountToken> authenticate(final String token) {
        try {
            logger.debug("[Token] Authenticating token");
            Builder builder = new Builder()
                .url(this.url);
            builder.addHeader("access_token", token);
            Request request = builder.build();
            Call call = this.client.newCall(request);
            try (Response response = call.execute()) {
                String responseBody = response.body().string();
                AccountToken account = parseJson(responseBody);
                String userId = account.getUser().getId();
                User user = userService.getById(userId);

                // Set activated date if not already
                if (user.getActivatedAt() == null) {
                    user.setActivatedAt(new Date());
                }

                // Update last seen at
                user.setLastSeenAt(new Date());
                userService.save(user);

                account.setUser(user);
                logger.info("[Token] Successfully authenticated user: {} ({})", account.getName(), user.getId());
                if ("0".equals(user.getId())) {
                    logger.error("User {} with login {} has an invalid user id (0)", user.getFullName(), account.getName());
                }
                return Optional.of(account);
            }

        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
