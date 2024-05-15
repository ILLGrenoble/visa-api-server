package eu.ill.visa.web.graphql.context;

import eu.ill.visa.security.tokens.AccountToken;
import graphql.kickstart.execution.context.DefaultGraphQLContext;
import org.dataloader.DataLoaderRegistry;

import java.util.HashMap;


public class AuthenticationContext extends DefaultGraphQLContext {
    private final AccountToken accountToken;

    public AuthenticationContext(DataLoaderRegistry dataLoaderRegistry, AccountToken accountToken) {
        super(dataLoaderRegistry, new HashMap<>());
        this.accountToken = accountToken;
    }

    public AccountToken getAccountToken() {
        return accountToken;
    }
}
