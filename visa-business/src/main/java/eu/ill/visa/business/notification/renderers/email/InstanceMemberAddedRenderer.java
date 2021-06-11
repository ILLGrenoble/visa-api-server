package eu.ill.visa.business.notification.renderers.email;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import eu.ill.visa.business.NotificationRendererException;
import eu.ill.visa.business.notification.NotificationRenderer;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceMember;
import eu.ill.visa.core.domain.User;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class InstanceMemberAddedRenderer implements NotificationRenderer {

    private final Instance instance;
    private final User owner;
    private final InstanceMember member;
    private final String rootURL;
    private final String emailTemplatesDirectory;

    public InstanceMemberAddedRenderer(final Instance instance,
                                       final String emailTemplatesDirectory,
                                       final User owner,
                                       final InstanceMember member,
                                       final String rootURL
    ) {
        this.instance = instance;
        this.owner = owner;
        this.member = member;
        this.rootURL = rootURL;
        this.emailTemplatesDirectory = emailTemplatesDirectory;
    }

    @Override
    public String render() throws NotificationRendererException {
        try {
            final PebbleEngine engine = new PebbleEngine.Builder().build();
            final PebbleTemplate compiledTemplate = engine.getTemplate(emailTemplatesDirectory + "instance-member-added.twig");
            final Writer writer = new StringWriter();
            final Map<String, Object> variables = new HashMap<>();
            variables.put("instance", instance);
            variables.put("owner", owner);
            variables.put("member", member);
            variables.put("rootURL", rootURL);
            compiledTemplate.evaluate(writer, variables);
            return writer.toString();
        } catch (IOException exception) {
            throw new NotificationRendererException(format("Unable to render template: %s", exception.getMessage()));
        }
    }
}
