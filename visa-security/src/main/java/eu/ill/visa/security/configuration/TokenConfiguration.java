package eu.ill.visa.security.configuration;

import jakarta.validation.constraints.NotNull;

public class TokenConfiguration {

    @NotNull
    private String prefix = "Bearer";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String accountsUrl;

    public String getAccountsUrl() {
        return accountsUrl;
    }

    public void setAccountsUrl(String accountsUrl) {
        this.accountsUrl = accountsUrl;
    }


}
