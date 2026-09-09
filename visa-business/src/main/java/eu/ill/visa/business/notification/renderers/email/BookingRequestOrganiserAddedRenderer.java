package eu.ill.visa.business.notification.renderers.email;

import eu.ill.visa.business.NotificationRendererException;
import eu.ill.visa.business.notification.NotificationRenderer;
import eu.ill.visa.core.entity.BookingRequest;
import eu.ill.visa.core.entity.User;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class BookingRequestOrganiserAddedRenderer extends BaseRenderer implements NotificationRenderer {

    private final BookingRequest bookingRequest;
    private final User organiser;
    private final String rootURL;
    private final String adminEmailAddress;
    private final String emailTemplatesDirectory;

    public BookingRequestOrganiserAddedRenderer(final BookingRequest bookingRequest,
                                                final User organiser,
                                                final String emailTemplatesDirectory,
                                                final String rootURL,
                                                final String adminEmailAddress) {
        this.bookingRequest = bookingRequest;
        this.organiser = organiser;
        this.rootURL = rootURL;
        this.adminEmailAddress = adminEmailAddress;
        this.emailTemplatesDirectory = emailTemplatesDirectory;
    }

    @Override
    public String render() throws NotificationRendererException {
        try {
            final PebbleTemplate compiledTemplate = this.getTemplate(emailTemplatesDirectory + "booking-request-organiser-added.twig");
            final Writer writer = new StringWriter();
            final Map<String, Object> variables = new HashMap<>();

            variables.put("bookingRequest", bookingRequest);
            variables.put("organiser", organiser);
            variables.put("rootURL", rootURL);
            variables.put("adminEmailAddress", adminEmailAddress);
            compiledTemplate.evaluate(writer, variables);
            return writer.toString();
        } catch (IOException exception) {
            throw new NotificationRendererException(format("Unable to render template: %s", exception.getMessage()));
        }
    }
}
