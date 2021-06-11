package eu.ill.visa.business.services;

import com.google.inject.Singleton;
import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.business.NotificationConfiguration;
import eu.ill.visa.business.notification.EmailNotificationAdapter;
import eu.ill.visa.business.notification.NotificationAdapter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
            adapters.add(createAdapter(config, instanceConfiguration, rootURL));
        }
        return new NotificationService(adapters);
    }

    public NotificationAdapter createAdapter(final NotificationConfiguration configuration,
                                             final InstanceConfiguration instanceConfiguration,
                                             final String rootURL) {
        final Map<String, String> parameters = configuration.getParameters();
        if (configuration.getAdapter().equals("email")) {
            if (configuration.isEnabled()) {
                return createEmailNotificationAdapter(parameters, rootURL, instanceConfiguration.getUserMaxInactivityDurationHours(), instanceConfiguration.getStaffMaxInactivityDurationHours(), instanceConfiguration.getUserMaxLifetimeDurationHours(), instanceConfiguration.getStaffMaxLifetimeDurationHours());
            }
        } else {
            throw new IllegalArgumentException(format("Unknown notification adapter: %s", configuration.getAdapter()));
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
        final String emailTemplatesDirectory = notNull(parameters.get("emailTemplatesDirectory"), "emailTemplatesDirectory must be set");
        return new EmailNotificationAdapter(host,
            port,
            fromEmailAddress,
            bccEmailAddress,
            adminEmailAddress,
            emailTemplatesDirectory,
            rootURL,
            userMaxInactivityDurationHours,
            staffMaxInactivityDurationHours,
            userMaxLifetimeDurationHours,
            staffMaxLifetimeDurationHours);
    }
}
