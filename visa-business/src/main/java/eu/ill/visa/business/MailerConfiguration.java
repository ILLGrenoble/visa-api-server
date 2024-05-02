package eu.ill.visa.business;

import java.util.Optional;

public interface MailerConfiguration {

    boolean enabled();

    Optional<String> rootURL();

    Optional<String> fromEmailAddress();

    Optional<String> bccEmailAddress();

    Optional<String> devEmailAddress();

    Optional<String> adminEmailAddress();

    String emailTemplatesDirectory();
}
