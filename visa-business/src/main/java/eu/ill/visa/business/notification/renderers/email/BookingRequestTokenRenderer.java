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

public class BookingRequestTokenRenderer extends BaseRenderer implements NotificationRenderer {

    private final BookingRequest bookingRequest;
    final User tokenOwner;
    private final String rootURL;
    private final String adminEmailAddress;
    private final String emailTemplatesDirectory;

    public BookingRequestTokenRenderer(final BookingRequest bookingRequest,
                                       final User tokenOwner,
                                       final String emailTemplatesDirectory,
                                       final String rootURL,
                                       final String adminEmailAddress) {
        this.bookingRequest = bookingRequest;
        this.tokenOwner = tokenOwner;
        this.rootURL = rootURL;
        this.adminEmailAddress = adminEmailAddress;
        this.emailTemplatesDirectory = emailTemplatesDirectory;
    }

    @Override
    public String render() throws NotificationRendererException {
        try {
            final PebbleTemplate compiledTemplate = this.getTemplate(emailTemplatesDirectory + "booking-request-token.twig");
            final Writer writer = new StringWriter();
            final Map<String, Object> variables = new HashMap<>();

            variables.put("bookingRequest", bookingRequest);
            variables.put("tokenOwner", tokenOwner);
            variables.put("rootURL", rootURL);
            variables.put("adminEmailAddress", adminEmailAddress);
            compiledTemplate.evaluate(writer, variables);
            return writer.toString();
        } catch (IOException exception) {
            throw new NotificationRendererException(format("Unable to render template: %s", exception.getMessage()));
        }
    }
}
