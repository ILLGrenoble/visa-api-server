package eu.ill.visa.business;

import io.quarkus.arc.Unremovable;
import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.MailerName;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ConfigurationProducer {

    private final BusinessConfiguration businessConfiguration;

    // Keep here so that the CDI bean is available when required by the Error Reporter
    @Inject
    @MailerName("logging")
    Mailer loggingMailer;

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

    @Produces
    @Unremovable
    public ErrorReportEmailConfiguration errorReportEmail() {
        return this.businessConfiguration.errorReportEmail();
    }
}
