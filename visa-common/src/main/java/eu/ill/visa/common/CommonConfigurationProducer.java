package eu.ill.visa.common;

import io.quarkus.arc.Unremovable;
import io.quarkus.mailer.MailerName;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CommonConfigurationProducer {

    private final CommonConfiguration commonConfiguration;

    // Keep here so that the CDI bean is available when required by the Error Reporter
    @Inject
    @MailerName("logging")
    ReactiveMailer loggingMailer;

    @Inject
    public CommonConfigurationProducer(final CommonConfiguration commonConfiguration) {
        this.commonConfiguration = commonConfiguration;
    }

    @Produces
    @Unremovable
    public ErrorReportEmailConfiguration errorReportEmail() {
        return this.commonConfiguration.errorReportEmail();
    }
}
