package eu.ill.visa.business;

public class SignatureConfiguration {

    private String privateKeyPath;
    private String publicKeyPath;

    public SignatureConfiguration() {

    }

    public SignatureConfiguration(String privateKeyPath, String publicKeyPath) {
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
