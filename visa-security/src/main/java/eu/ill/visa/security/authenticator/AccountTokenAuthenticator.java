package eu.ill.visa.security.authenticator;

import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.security.tokens.AccountToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

@ApplicationScoped
public class AccountTokenAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(AccountTokenAuthenticator.class);

    private final UserService userService;

    @RestClient
    AccountsServiceClient accountsServiceClient;

    @Inject
    public AccountTokenAuthenticator(final UserService userService) {
        this.userService = userService;
    }

    public Optional<AccountToken> authenticate(final String token) {
        logger.debug("[Token] Authenticating token");

        final AccountToken accountToken = this.getAccountToken(token);

        if (accountToken != null) {
            if (accountToken.getUser() != null) {
                final User user = getOrCreateUser(accountToken.getUser());
                accountToken.setUser(user);

                if ("0".equals(user.getId())) {
                    logger.warn("[Token] User {} with login {} has an invalid user id (0)", user.getFullName(), accountToken.getName());

                } else {
                    logger.debug("[Token] Successfully authenticated user: {} ({})", accountToken.getName(), user.getId());
                }

                return Optional.of(accountToken);

            } else {
                logger.error("[Token] Did not obtain a valid user from the Account Service for username {}", accountToken.getName());
            }
        }

        return Optional.empty();
    }

    private AccountToken getAccountToken(final String token) {

        try {
            // Make REST API call to Accounts Service
            return this.accountsServiceClient.getAccountToken(token);
        } catch (RuntimeException runtimeException) {
            logger.error(runtimeException.getMessage());
        } catch (Exception e) {
            logger.error("[Token] Error obtaining account from access token: {}", e.getMessage());
        }

        return null;
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
            .instanceQuota(this.userService.getDefaultInstanceQuota())
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
        user = userService.getByIdWithRoles(id);
        if (user == null) {
            user = createUserFromData(id, firstName, lastName, email);
        }

        // Set activated date if not already
        if (user.getActivatedAt() == null) {
            user.setActivatedAt(new Date());
        }

        user.setLastSeenAt(new Date());
        userService.save(user);
        return user;
    }


}
