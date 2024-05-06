package eu.ill.visa.web.graphql.context;

import eu.ill.visa.security.tokens.AccountToken;
import graphql.kickstart.execution.context.DefaultGraphQLContext;
import org.dataloader.DataLoaderRegistry;

import javax.security.auth.Subject;


public class AuthenticationContext extends DefaultGraphQLContext {
    private final AccountToken accountToken;

    public AuthenticationContext(DataLoaderRegistry dataLoaderRegistry, Subject subject, AccountToken accountToken) {
        super(dataLoaderRegistry, subject);
        this.accountToken = accountToken;
    }

    public AccountToken getAccountToken() {
        return accountToken;
    }
}
