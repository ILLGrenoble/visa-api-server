package eu.ill.visa.web.rest;

public interface LoginConfiguration {

    String issuer();

    String clientId();

    String scope();

    boolean showDebugInformation();

    boolean sessionChecksEnabled();
}
