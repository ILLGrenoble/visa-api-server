package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "business.mailer", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface MailerConfiguration {

    boolean enabled();

    Optional<String> rootURL();

    Optional<String> fromEmailAddress();

    Optional<String> bccEmailAddress();

    Optional<String> devEmailAddress();

    Optional<String> adminEmailAddress();

    String emailTemplatesDirectory();
}
