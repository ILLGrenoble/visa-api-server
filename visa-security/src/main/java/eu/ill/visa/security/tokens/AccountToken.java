package eu.ill.visa.security.tokens;


import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.core.entity.User;

import java.security.Principal;
import java.util.Map;

public class AccountToken implements Principal {

    @JsonProperty("username")
    private String name;

    private User user;

    private Map<String, String> accountParameters;

    public AccountToken() {
    }

    public AccountToken(final String username, final User user, final Map<String, String> accountParameters) {
        this.name = username;
        this.user = user;
        this.accountParameters = accountParameters;
    }

    @Override
    public String getName() {
        return name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<String, String> getAccountParameters() {
        return accountParameters;
    }

}

