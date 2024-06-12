package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "business", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface BusinessConfiguration {

    Integer numberInstanceActionThreads();

    MailerConfiguration mailer();

    InstanceConfiguration instance();

    SignatureConfiguration signature();

    SecurityGroupServiceClientConfiguration securityGroupServiceClient();

    ErrorReportEmailConfiguration errorReportEmail();
}
