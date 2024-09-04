package eu.ill.visa.business.notification.renderers.email;

import eu.ill.visa.business.NotificationRendererException;
import eu.ill.visa.business.notification.NotificationRenderer;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class InstanceLifetimeEmailRenderer extends BaseRenderer implements NotificationRenderer {

    private final Instance           instance;
    private final User               user;
    private final String             emailTemplatesDirectory;
    private final String             rootURL;
    private final String             adminEmailAddress;
    private final Long               userMaxInactivityDurationDays;
    private final Long               staffMaxInactivityDurationDays;
    private final Long               userMaxLifetimeDurationDays;
    private final Long               staffMaxLifetimeDurationDays;

    public InstanceLifetimeEmailRenderer(final Instance instance,
                                         final User user,
                                         final String emailTemplatesDirectory,
                                         final String rootURL,
                                         final String adminEmailAddress,
                                         final Integer userMaxInactivityDurationHours,
                                         final Integer staffMaxInactivityDurationHours,
                                         final Integer userMaxLifetimeDurationHours,
                                         final Integer staffMaxLifetimeDurationHours
    ) {
        this.instance = instance;
        this.user = user;
        this.emailTemplatesDirectory = emailTemplatesDirectory;
        this.rootURL = rootURL;
        this.adminEmailAddress = adminEmailAddress;
        this.userMaxInactivityDurationDays = TimeUnit.HOURS.toDays(userMaxInactivityDurationHours);
        this.staffMaxInactivityDurationDays = TimeUnit.HOURS.toDays(staffMaxInactivityDurationHours);
        this.userMaxLifetimeDurationDays = TimeUnit.HOURS.toDays(userMaxLifetimeDurationHours);
        this.staffMaxLifetimeDurationDays = TimeUnit.HOURS.toDays(staffMaxLifetimeDurationHours);
    }
    @Override
    public String render() throws NotificationRendererException {
        try {
            final PebbleTemplate compiledTemplate = this.getTemplate(emailTemplatesDirectory + "instance-lifetime.twig");
            final Writer writer = new StringWriter();
            final Map<String, Object> variables = new HashMap<>();

            final Long maxInactivityDurationDays = user.hasRole(Role.STAFF_ROLE) ? staffMaxInactivityDurationDays : userMaxInactivityDurationDays;
            final Long maxLifetimeDurationDays = user.hasRole(Role.STAFF_ROLE) ? staffMaxLifetimeDurationDays : userMaxLifetimeDurationDays;
            variables.put("instance", instance);
            variables.put("user", user);
            variables.put("isStaff", user.hasRole(Role.STAFF_ROLE));
            variables.put("maxInactivityDurationDays", maxInactivityDurationDays);
            variables.put("maxLifetimeDurationDays", maxLifetimeDurationDays);
            variables.put("rootURL", rootURL);
            variables.put("adminEmailAddress", adminEmailAddress);
            compiledTemplate.evaluate(writer, variables);
            return writer.toString();
        } catch (IOException exception) {
            throw new NotificationRendererException(format("Unable to render template: %s", exception.getMessage()));
        }
    }
}
