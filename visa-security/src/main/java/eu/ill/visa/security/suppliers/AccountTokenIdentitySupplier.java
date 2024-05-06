package eu.ill.visa.security.suppliers;

import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.security.SecurityConfiguration;
import eu.ill.visa.security.tokens.AccountToken;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Date;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Dependent
public class AccountTokenIdentitySupplier implements Supplier<SecurityIdentity> {

    private static final Logger logger = LoggerFactory.getLogger(AccountTokenIdentitySupplier.class);

    private final UserService userService;
    private final AccountsServiceClient accountsServiceClient;

    private TokenCredential token;

    @Inject
    AccountTokenIdentitySupplier(final UserService userService,
                                 final SecurityConfiguration configuration) {
        this.userService = userService;
        this.accountsServiceClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.tokenConfiguration().accountsUrl()))
            .clientHeadersFactory((MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) -> {
                MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
                result.add("x-access-token", this.token.getToken());
                return result;
            })
            .build(AccountsServiceClient.class);
    }

    public void setToken(TokenCredential token) {
        this.token = token;
    }

    @ActivateRequestContext
    public SecurityIdentity get() {
        if (token != null) {
            logger.debug("[Token] Authenticating token");

            final AccountToken accountToken = this.getAccountToken();

            if (accountToken != null) {
                if (accountToken.getUser() != null) {
                    final User user = getOrCreateUser(accountToken.getUser());
                    accountToken.setUser(user);

                    if ("0".equals(user.getId())) {
                        logger.warn("[Token] User {} with login {} has an invalid user id (0)", user.getFullName(), accountToken.getName());

                    } else {
                        logger.info("[Token] Successfully authenticated user: {} ({})", accountToken.getName(), user.getId());
                    }

                    // Create SecurityIdentity with AccountToken as Principal and User Roles
                    return QuarkusSecurityIdentity.builder()
                        .setPrincipal(accountToken)
                        .addRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                        .setAnonymous(false)
                        .build();

                } else {
                    logger.error("[Token] Did not obtain a valid user from the Account Service for username {}", accountToken.getName());
                }
            }

        }

        throw new AuthenticationFailedException("invalid token");
    }

    private AccountToken getAccountToken() {

        try {
            // Make REST API call to Accounts Service
            return this.accountsServiceClient.getAccountToken();
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
        user = userService.getById(id);
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
