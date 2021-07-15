package eu.ill.visa.business;

import javax.validation.constraints.NotNull;

public class SignatureConfiguration {

    @NotNull
    private String privateKeyPath;
    @NotNull
    private String publicKeyPath;

    public SignatureConfiguration() {

    }

    public SignatureConfiguration(@NotNull String privateKeyPath, @NotNull String publicKeyPath) {
        this.privateKeyPath = privateKeyPath;
        this.publicKeyPath = publicKeyPath;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }
}
