package eu.ill.visa.business.notification.renderers.email;

import eu.ill.visa.business.NotificationRendererException;
import eu.ill.visa.business.notification.NotificationRenderer;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class InstanceExtensionRequestRenderer extends BaseRenderer implements NotificationRenderer {

    private final Instance instance;
    private final User owner;
    private final String comments;
    private final String rootURL;
    private final String emailTemplatesDirectory;

    public InstanceExtensionRequestRenderer(final Instance instance,
                                            final String emailTemplatesDirectory,
                                            final User owner,
                                            final String comments,
                                            final String rootURL) {
        this.instance = instance;
        this.owner = owner;
        this.comments = comments;
        this.rootURL = rootURL;
        this.emailTemplatesDirectory = emailTemplatesDirectory;
    }

    @Override
    public String render() throws NotificationRendererException {
        try {
            final PebbleTemplate compiledTemplate = this.getTemplate(emailTemplatesDirectory + "instance-extension-request.twig");
            final Writer writer = new StringWriter();
            final Map<String, Object> variables = new HashMap<>();
            variables.put("instance", instance);
            variables.put("owner", owner);
            variables.put("comments", comments.replaceAll("\n", "<br>"));
            variables.put("rootURL", rootURL);
            compiledTemplate.evaluate(writer, variables);
            return writer.toString();
        } catch (IOException exception) {
            throw new NotificationRendererException(format("Unable to render template: %s", exception.getMessage()));
        }
    }
}
