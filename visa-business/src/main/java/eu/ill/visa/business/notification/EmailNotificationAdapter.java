package eu.ill.visa.business.notification;

import eu.ill.visa.business.NotificationRendererException;
import eu.ill.visa.business.notification.renderers.email.*;
import eu.ill.visa.core.domain.*;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

import static eu.ill.visa.core.domain.enumerations.InstanceMemberRole.OWNER;
import static java.lang.String.format;
import static org.simplejavamail.api.mailer.config.TransportStrategy.SMTP;

public class EmailNotificationAdapter implements NotificationAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationAdapter.class);
    private final Mailer mailer;
    private final String host;
    private final Integer port;
    private final String fromEmailAddress;
    private final String bccEmailAddress;
    private final String adminEmailAddress;
    private final String devEmailAddress;
    private final String emailTemplatesDirectory;
    private final String rootURL;
    private final Integer userMaxInactivityDurationHours;
    private final Integer staffMaxInactivityDurationHours;
    private final Integer userMaxLifetimeDurationHours;
    private final Integer staffMaxLifetimeDurationHours;

    public EmailNotificationAdapter(final String host,
                                    final Integer port,
                                    final String fromEmailAddress,
                                    final String bccEmailAddress,
                                    final String adminEmailAddress,
                                    final String devEmailAddress,
                                    final String emailTemplatesDirectory,
                                    final String rootURL,
                                    final Integer userMaxInactivityDurationHours,
                                    final Integer staffMaxInactivityDurationHours,
                                    final Integer userMaxLifetimeDurationHours,
                                    final Integer staffMaxLifetimeDurationHours) {
        this.host = host;
        this.port = port;
        this.fromEmailAddress = fromEmailAddress;
        this.bccEmailAddress = bccEmailAddress;
        this.adminEmailAddress = adminEmailAddress;
        this.devEmailAddress = devEmailAddress;
        this.emailTemplatesDirectory = emailTemplatesDirectory;
        this.rootURL = rootURL;
        this.userMaxInactivityDurationHours = userMaxInactivityDurationHours;
        this.staffMaxInactivityDurationHours = staffMaxInactivityDurationHours;
        this.userMaxLifetimeDurationHours = userMaxLifetimeDurationHours;
        this.staffMaxLifetimeDurationHours = staffMaxLifetimeDurationHours;
        this.mailer = createMailer();
    }

    private Mailer createMailer() {
        return MailerBuilder
            .withSMTPServer(host, port)
            .withTransportStrategy(SMTP)
            .withSessionTimeout(10 * 1000)
            .clearEmailAddressCriteria()
            .buildMailer();
    }

    private Email buildEmail(final String recipient, final String subject, final String html) {
        final EmailPopulatingBuilder emailBuilder = EmailBuilder.startingBlank()
            .from(fromEmailAddress)
            .to(recipient)
            .withSubject(subject)
            .withHTMLText(html);

        if (this.bccEmailAddress != null) {
            emailBuilder.bcc(this.bccEmailAddress);
        }
        return emailBuilder.buildEmail();
    }

    @Override
    public void sendInstanceExpiringNotification(final Instance instance, final Date expirationDate) {
        try {
            final Optional<InstanceMember> member = instance.getMembers().stream()
                .filter(object -> object.isRole(OWNER))
                .findFirst();
            if (member.isPresent()) {
                final User user = member.get().getUser();
                final String subject = "[VISA] Your instance will soon expire due to inactivity";
                final NotificationRenderer renderer = new InstanceExpiringEmailRenderer(instance, expirationDate, user, emailTemplatesDirectory, rootURL, adminEmailAddress, userMaxInactivityDurationHours, staffMaxInactivityDurationHours, userMaxLifetimeDurationHours, staffMaxLifetimeDurationHours);
                final Email email = buildEmail(user.getEmail(), subject, renderer.render());
                mailer.sendMail(email);
            } else {
                logger.error("Unable to find owner for instance: {}", instance.getId());
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (MailException exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }

    @Override
    public void sendInstanceDeletedNotification(Instance instance, InstanceExpiration instanceExpiration) {
        try {
            final Optional<InstanceMember> member = instance.getMembers().stream()
                .filter(object -> object.isRole(OWNER))
                .findFirst();
            if (member.isPresent()) {
                final User user = member.get().getUser();
                final String subject = "[VISA] Your instance has been deleted";
                final NotificationRenderer renderer = new InstanceDeletedEmailRenderer(instance, instanceExpiration, user, emailTemplatesDirectory, rootURL, adminEmailAddress, userMaxInactivityDurationHours, staffMaxInactivityDurationHours, userMaxLifetimeDurationHours, staffMaxLifetimeDurationHours);
                final Email email = buildEmail(user.getEmail(), subject, renderer.render());
                mailer.sendMail(email);
            } else {
                logger.error("Unable to find owner for instance: {}", instance.getId());
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (MailException exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }

    @Override
    public void sendInstanceLifetimeNotification(Instance instance) {
        try {
            final Optional<InstanceMember> member = instance.getMembers().stream()
                .filter(object -> object.isRole(OWNER))
                .findFirst();
            if (member.isPresent()) {
                final User user = member.get().getUser();
                final String subject = "[VISA] Your instance will soon be deleted due to reaching its maximum lifetime";
                final NotificationRenderer renderer = new InstanceLifetimeEmailRenderer(instance, user, emailTemplatesDirectory, rootURL, adminEmailAddress, userMaxInactivityDurationHours, staffMaxInactivityDurationHours, userMaxLifetimeDurationHours, staffMaxLifetimeDurationHours);
                final Email email = buildEmail(user.getEmail(), subject, renderer.render());
                mailer.sendMail(email);
            } else {
                logger.error("Unable to find owner for instance: {}", instance.getId());
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (MailException exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }

    @Override
    public void sendInstanceMemberAddedNotification(Instance instance, InstanceMember member) {
        try {
            if (!member.isRole(OWNER)) {
                final Optional<InstanceMember> owner = instance.getMembers().stream()
                    .filter(object -> object.isRole(OWNER))
                    .findFirst();
                if (owner.isPresent()) {
                    final String subject = "[VISA] You have been added as a member to an instance";
                    final NotificationRenderer renderer = new InstanceMemberAddedRenderer(instance, emailTemplatesDirectory, owner.get().getUser(), member, rootURL, adminEmailAddress);
                    final Email email = buildEmail(member.getUser().getEmail(), subject, renderer.render());
                    mailer.sendMail(email);
                } else {
                    logger.error("Unable to find owner for instance: {}", instance.getId());
                }
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (MailException exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }

    @Override
    public void sendInstanceCreatedNotification(final Instance instance) {
        if (!devEmailAddress.isEmpty()) {
            try {
                final Optional<InstanceMember> member = instance.getMembers().stream()
                    .filter(object -> object.isRole(OWNER))
                    .findFirst();
                if (member.isPresent()) {
                    final User user = member.get().getUser();
                    final Plan plan = instance.getPlan();
                    final Flavour flavour = plan.getFlavour();
                    final String text = format("%s created a new instance '%s' (%d) with the flavour of %s",
                        user.getFullName(),
                        instance.getName(),
                        instance.getId(),
                        flavour.getName()
                    );
                    final Email email = EmailBuilder.startingBlank()
                        .from(fromEmailAddress)
                        .to(devEmailAddress)
                        .withSubject("[VISA] A new instance has been created")
                        .withPlainText(text)
                        .buildEmail();
                    mailer.sendMail(email);
                } else {
                    logger.error("Unable to find owner for instance: {}", instance.getId());
                }
            } catch (MailException exception) {
                logger.error("Error sending email: {}", exception.getMessage());
            }
        } else {
            logger.warn("Unable to send instance created email, $VISA_NOTIFICATION_EMAIL_ADAPTER_ADMIN_EMAIL_ADDRESS is not configured");
        }
    }

    @Override
    public void sendInstanceExtensionRequestNotification(final Instance instance, final String comments) {
        if (!adminEmailAddress.isEmpty()) {
            try {
                final Optional<InstanceMember> member = instance.getMembers().stream()
                    .filter(object -> object.isRole(OWNER))
                    .findFirst();
                if (member.isPresent()) {
                    final User owner = member.get().getUser();

                    final String subject = "[VISA] An instance extension request has been made";
                    final NotificationRenderer renderer = new InstanceExtensionRequestRenderer(instance, emailTemplatesDirectory, owner, comments, rootURL);
                    final Email email = buildEmail(adminEmailAddress, subject, renderer.render());
                    mailer.sendMail(email);

                } else {
                    logger.error("Unable to find owner for instance: {}", instance.getId());
                }
            } catch (MailException exception) {
                logger.error("Error sending email: {}", exception.getMessage());

            } catch (NotificationRendererException exception) {
                logger.error("Error rendering email : {}", exception.getMessage());
            }
        } else {
            logger.warn("Unable to send instance created email, $VISA_NOTIFICATION_EMAIL_ADAPTER_ADMIN_EMAIL_ADDRESS is not configured");
        }
    }

    @Override
    public void sendInstanceExtensionNotification(final Instance instance, boolean extensionGranted, final String handlerComments) {
        try {
            final Optional<InstanceMember> member = instance.getMembers().stream()
                .filter(object -> object.isRole(OWNER))
                .findFirst();
            if (member.isPresent()) {
                final User user = member.get().getUser();
                final String subject = extensionGranted ? "[VISA] Your instance has been granted an extended lifetime" : "[VISA] Your instance has been refused an extended lifetime";
                final NotificationRenderer renderer = new InstanceExtensionRenderer(instance, extensionGranted, handlerComments, user, emailTemplatesDirectory, rootURL, adminEmailAddress, userMaxInactivityDurationHours, staffMaxInactivityDurationHours);
                final Email email = buildEmail(user.getEmail(), subject, renderer.render());
                mailer.sendMail(email);
            } else {
                logger.error("Unable to find owner for instance: {}", instance.getId());
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (MailException exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }


}
