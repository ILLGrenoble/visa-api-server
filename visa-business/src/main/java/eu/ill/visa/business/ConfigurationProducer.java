package eu.ill.visa.business;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ConfigurationProducer {

    private final BusinessConfiguration businessConfiguration;

    @Inject
    public ConfigurationProducer(final BusinessConfiguration businessConfiguration) {
        this.businessConfiguration = businessConfiguration;
    }

    @Produces
    public MailerConfiguration mailer() {
        return this.businessConfiguration.mailer();
    }

    @Produces
    public InstanceConfiguration instance() {
        return this.businessConfiguration.instance();
    }

    @Produces
    public SignatureConfiguration signature() {
        return this.businessConfiguration.signature();
    }

    @Produces
    public SecurityGroupServiceClientConfiguration securityGroupServiceClient() {
        return this.businessConfiguration.securityGroupServiceClient();
    }
}
