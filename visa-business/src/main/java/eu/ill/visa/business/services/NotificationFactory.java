package eu.ill.visa.business.services;

import jakarta.inject.Singleton;
import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.business.NotificationConfiguration;
import eu.ill.visa.business.notification.EmailNotificationAdapter;
import eu.ill.visa.business.notification.NotificationAdapter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

@Singleton
public class NotificationFactory {

    public NotificationFactory() {
    }

    public NotificationService getNotificationService(final List<NotificationConfiguration> configuration,
                                                      final InstanceConfiguration instanceConfiguration,
                                                      final String rootURL) {
        final List<NotificationAdapter> adapters = new ArrayList<>();
        for (NotificationConfiguration config : configuration) {
            NotificationAdapter adapter = createAdapter(config, instanceConfiguration, rootURL);
            if (adapter != null) {
                adapters.add(adapter);
            }
        }
        return new NotificationService(adapters);
    }

    public NotificationAdapter createAdapter(final NotificationConfiguration configuration,
                                             final InstanceConfiguration instanceConfiguration,
                                             final String rootURL) {
        final Map<String, String> parameters = configuration.parameters();
        if (configuration.adapter().equals("email")) {
            if (configuration.enabled()) {
                return createEmailNotificationAdapter(parameters, rootURL, instanceConfiguration.userMaxInactivityDurationHours(), instanceConfiguration.staffMaxInactivityDurationHours(), instanceConfiguration.userMaxLifetimeDurationHours(), instanceConfiguration.staffMaxLifetimeDurationHours());
            }
        } else {
            throw new IllegalArgumentException(format("Unknown notification adapter: %s", configuration.adapter()));
        }
        return null;
    }

    private EmailNotificationAdapter createEmailNotificationAdapter(final Map<String, String> parameters,
                                                                    @NotNull final String rootURL,
                                                                    @NotNull @Valid Integer userMaxInactivityDurationHours,
                                                                    @NotNull @Valid Integer staffMaxInactivityDurationHours,
                                                                    @NotNull @Valid Integer userMaxLifetimeDurationHours,
                                                                    @NotNull @Valid Integer staffMaxLifetimeDurationHours) {
        final String host = notNull(parameters.get("host"), "host must be set");
        final Integer port = parseInt(notNull(parameters.get("port"), "port must be set"));
        final String fromEmailAddress = notNull(parameters.get("fromEmailAddress"), "fromEmailAddress must be set");
        final String bccEmailAddress = parameters.get("bccEmailAddress");
        final String adminEmailAddress = notNull(parameters.get("adminEmailAddress"), "adminEmailAddress must be set");
        final String devEmailAddress = parameters.get("devEmailAddress");
        String emailTemplatesDirectory = notNull(parameters.get("emailTemplatesDirectory"), "emailTemplatesDirectory must be set");

        // Ensure email templates directory has a trailing /
        if (!emailTemplatesDirectory.endsWith("/")) {
            emailTemplatesDirectory = emailTemplatesDirectory + "/";
        }

        return new EmailNotificationAdapter(host,
            port,
            fromEmailAddress,
            bccEmailAddress,
            adminEmailAddress,
            devEmailAddress,
            emailTemplatesDirectory,
            rootURL,
            userMaxInactivityDurationHours,
            staffMaxInactivityDurationHours,
            userMaxLifetimeDurationHours,
            staffMaxLifetimeDurationHours);
    }
}
