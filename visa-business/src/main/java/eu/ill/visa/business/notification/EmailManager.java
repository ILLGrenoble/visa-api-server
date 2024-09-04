package eu.ill.visa.business.notification;

import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.business.MailerConfiguration;
import eu.ill.visa.business.NotificationRendererException;
import eu.ill.visa.business.notification.renderers.email.*;
import eu.ill.visa.business.services.InstanceMemberService;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static java.lang.String.format;

@Singleton
public class EmailManager {

    private static final Logger logger = LoggerFactory.getLogger(EmailManager.class);

    private final Mailer mailer;

    private final boolean enabled;
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

    private final InstanceMemberService instanceMemberService;

    @Inject
    public EmailManager(final MailerConfiguration mailerConfiguration,
                        final InstanceConfiguration instanceConfiguration,
                        final InstanceMemberService instanceMemberService,
                        final Mailer mailer) {

        this.instanceMemberService = instanceMemberService;

        this.enabled = mailerConfiguration.enabled();
        this.rootURL = mailerConfiguration.rootURL().orElse(null);
        this.fromEmailAddress = mailerConfiguration.fromEmailAddress().orElse(null);
        this.bccEmailAddress = mailerConfiguration.bccEmailAddress().orElse(null);
        this.adminEmailAddress = mailerConfiguration.adminEmailAddress().orElse(null);
        this.devEmailAddress = mailerConfiguration.devEmailAddress().orElse(null);

        // Ensure email templates directory has a trailing /
        if (!mailerConfiguration.emailTemplatesDirectory().endsWith("/")) {
            emailTemplatesDirectory = mailerConfiguration.emailTemplatesDirectory() + "/";
        } else {
            emailTemplatesDirectory = mailerConfiguration.emailTemplatesDirectory();
        }

        this.userMaxInactivityDurationHours = instanceConfiguration.userMaxInactivityDurationHours();
        this.staffMaxInactivityDurationHours = instanceConfiguration.staffMaxInactivityDurationHours();
        this.userMaxLifetimeDurationHours = instanceConfiguration.userMaxLifetimeDurationHours();
        this.staffMaxLifetimeDurationHours = instanceConfiguration.staffMaxLifetimeDurationHours();

        this.mailer = mailer;
    }

    private Mail buildEmail(final String recipient, final String subject, final String html) {
        final Mail email = Mail.withHtml(recipient, subject,  html);
        email.setFrom(this.fromEmailAddress);

        if (this.bccEmailAddress != null) {
            email.addBcc(this.bccEmailAddress);
        }
        return email;
    }

    private Mail buildEmail(final String recipient, final String cc, final String subject, final String html) {
        final Mail email = Mail.withHtml(recipient, subject,  html);
        email.addCc(cc);
        email.setFrom(this.fromEmailAddress);

        if (this.bccEmailAddress != null) {
            email.addBcc(this.bccEmailAddress);
        }
        return email;
    }

    private void sendMail(Mail mail) {
        if (this.enabled) {
            this.mailer.send(mail);
        }
    }

    public void sendInstanceExpiringNotification(final Instance instance, final Date expirationDate) {
        try {
            final User owner = this.instanceMemberService.getOwnerByInstanceId(instance.getId());
            if (owner != null) {
                final String subject = "[VISA] Your instance will soon expire due to inactivity";
                final NotificationRenderer renderer = new InstanceExpiringEmailRenderer(instance, expirationDate, owner, emailTemplatesDirectory, rootURL, adminEmailAddress, userMaxInactivityDurationHours, staffMaxInactivityDurationHours, userMaxLifetimeDurationHours, staffMaxLifetimeDurationHours);
                final Mail email = buildEmail(owner.getEmail(), subject, renderer.render());
                this.sendMail(email);
            } else {
                logger.error("Unable to find owner for instance: {}", instance.getId());
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (Exception exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }

    public void sendInstanceDeletedNotification(Instance instance, InstanceExpiration instanceExpiration) {
        try {
            final User owner = this.instanceMemberService.getOwnerByInstanceId(instance.getId());
            if (owner != null) {
                final String subject = "[VISA] Your instance has been deleted";
                final NotificationRenderer renderer = new InstanceDeletedEmailRenderer(instance, instanceExpiration, owner, emailTemplatesDirectory, rootURL, adminEmailAddress, userMaxInactivityDurationHours, staffMaxInactivityDurationHours, userMaxLifetimeDurationHours, staffMaxLifetimeDurationHours);
                final Mail email = buildEmail(owner.getEmail(), subject, renderer.render());
                this.sendMail(email);
            } else {
                logger.error("Unable to find owner for instance: {}", instance.getId());
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (Exception exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }

    public void sendInstanceLifetimeNotification(Instance instance) {
        try {
            final User owner = this.instanceMemberService.getOwnerByInstanceId(instance.getId());
            if (owner != null) {
                final String subject = "[VISA] Your instance will soon be deleted due to reaching its maximum lifetime";
                final NotificationRenderer renderer = new InstanceLifetimeEmailRenderer(instance, owner, emailTemplatesDirectory, rootURL, adminEmailAddress, userMaxInactivityDurationHours, staffMaxInactivityDurationHours, userMaxLifetimeDurationHours, staffMaxLifetimeDurationHours);
                final Mail email = buildEmail(owner.getEmail(), subject, renderer.render());
                this.sendMail(email);
            } else {
                logger.error("Unable to find owner for instance: {}", instance.getId());
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (Exception exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }

    public void sendInstanceMemberAddedNotification(Instance instance, InstanceMember member) {
        try {
            if (!member.isRole(InstanceMemberRole.OWNER)) {
                final User owner = this.instanceMemberService.getOwnerByInstanceId(instance.getId());
                if (owner != null) {
                    final String subject = "[VISA] You have been added as a member to an instance";
                    final NotificationRenderer renderer = new InstanceMemberAddedRenderer(instance, emailTemplatesDirectory, owner, member, rootURL, adminEmailAddress);
                    final Mail email = buildEmail(owner.getEmail(), subject, renderer.render());
                    this.sendMail(email);
                } else {
                    logger.error("Unable to find owner for instance: {}", instance.getId());
                }
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());
        } catch (Exception exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }

    public void sendInstanceCreatedNotification(final Instance instance) {
        if (!devEmailAddress.isEmpty()) {
            try {
                final User owner = this.instanceMemberService.getOwnerByInstanceId(instance.getId());
                if (owner != null) {
                    final Plan plan = instance.getPlan();
                    final Flavour flavour = plan.getFlavour();
                    final String text = format("%s created a new instance '%s' (%d) with the flavour of %s",
                        owner.getFullName(),
                        instance.getName(),
                        instance.getId(),
                        flavour.getName()
                    );

                    final Mail email = Mail.withText(this.devEmailAddress, "[VISA] A new instance has been created", text);
                    email.setFrom(this.fromEmailAddress);

                    this.sendMail(email);
                } else {
                    logger.error("Unable to find owner for instance: {}", instance.getId());
                }
            } catch (Exception exception) {
                logger.error("Error sending email: {}", exception.getMessage());
            }
        } else {
            logger.warn("Unable to send instance created email, $VISA_NOTIFICATION_EMAIL_ADAPTER_ADMIN_EMAIL_ADDRESS is not configured");
        }
    }

    public void sendInstanceExtensionRequestNotification(final Instance instance, final String comments) {
        if (!adminEmailAddress.isEmpty()) {
            try {
                final User owner = this.instanceMemberService.getOwnerByInstanceId(instance.getId());
                if (owner != null) {
                    final String subject = "[VISA] An instance extension request has been made";
                    final NotificationRenderer renderer = new InstanceExtensionRequestRenderer(instance, emailTemplatesDirectory, owner, comments, rootURL);
                    final Mail email = buildEmail(adminEmailAddress, subject, renderer.render());
                    this.sendMail(email);

                } else {
                    logger.error("Unable to find owner for instance: {}", instance.getId());
                }
            } catch (NotificationRendererException exception) {
                logger.error("Error rendering email : {}", exception.getMessage());

            } catch (Exception exception) {
                logger.error("Error sending email: {}", exception.getMessage());
            }
        } else {
            logger.warn("Unable to send instance created email, $VISA_NOTIFICATION_EMAIL_ADAPTER_ADMIN_EMAIL_ADDRESS is not configured");
        }
    }

    public void sendInstanceExtensionNotification(final Instance instance, boolean extensionGranted, final String handlerComments) {
        try {
            final User owner = this.instanceMemberService.getOwnerByInstanceId(instance.getId());
            if (owner != null) {
                final String subject = extensionGranted ? "[VISA] Your instance has been granted an extended lifetime" : "[VISA] Your instance has been refused an extended lifetime";
                final NotificationRenderer renderer = new InstanceExtensionRenderer(instance, extensionGranted, handlerComments, owner, emailTemplatesDirectory, rootURL, adminEmailAddress, userMaxInactivityDurationHours, staffMaxInactivityDurationHours);
                final Mail email = buildEmail(owner.getEmail(), adminEmailAddress, subject, renderer.render());
                this.sendMail(email);
            } else {
                logger.error("Unable to find owner for instance: {}", instance.getId());
            }
        } catch (NotificationRendererException exception) {
            logger.error("Error rendering email : {}", exception.getMessage());

        } catch (Exception exception) {
            logger.error("Error sending email: {}", exception.getMessage());
        }
    }


}
