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

public class InstanceExtensionRenderer extends BaseRenderer implements NotificationRenderer {

    private final Instance instance;
    private final boolean extensionGranted;
    private final String handlerComments;
    private final User user;
    private final String rootURL;
    private final String adminEmailAddress;
    private final String emailTemplatesDirectory;
    private final Long userMaxInactivityDurationDays;
    private final Long staffMaxInactivityDurationDays;

    public InstanceExtensionRenderer(final Instance instance,
                                     final boolean extensionGranted,
                                     final String handlerComments,
                                     final User user,
                                     final String emailTemplatesDirectory,
                                     final String rootURL,
                                     final String adminEmailAddress,
                                     final Integer userMaxInactivityDurationHours,
                                     final Integer staffMaxInactivityDurationHours) {
        this.instance = instance;
        this.extensionGranted = extensionGranted;
        this.handlerComments = handlerComments;
        this.user = user;
        this.rootURL = rootURL;
        this.adminEmailAddress = adminEmailAddress;
        this.emailTemplatesDirectory = emailTemplatesDirectory;
        this.userMaxInactivityDurationDays = TimeUnit.HOURS.toDays(userMaxInactivityDurationHours);
        this.staffMaxInactivityDurationDays = TimeUnit.HOURS.toDays(staffMaxInactivityDurationHours);
    }

    @Override
    public String render() throws NotificationRendererException {
        try {
            final PebbleTemplate compiledTemplate = this.getTemplate(emailTemplatesDirectory + "instance-extension.twig");
            final Writer writer = new StringWriter();
            final Map<String, Object> variables = new HashMap<>();

            final Long maxInactivityDurationDays = user.hasRole(Role.STAFF_ROLE) ? staffMaxInactivityDurationDays : userMaxInactivityDurationDays;

            variables.put("instance", instance);
            variables.put("handlerComments", handlerComments == null ? "" : handlerComments.replaceAll("\n", "<br>"));
            variables.put("accepted", extensionGranted);
            variables.put("maxInactivityDurationDays", maxInactivityDurationDays);
            variables.put("user", user);
            variables.put("rootURL", rootURL);
            variables.put("adminEmailAddress", adminEmailAddress);
            compiledTemplate.evaluate(writer, variables);
            return writer.toString();
        } catch (IOException exception) {
            throw new NotificationRendererException(format("Unable to render template: %s", exception.getMessage()));
        }
    }
}
