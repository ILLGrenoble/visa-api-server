package eu.ill.visa.business.notification;

import eu.ill.visa.business.notification.handler.EmailHandler;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceExpiration;
import eu.ill.visa.core.entity.InstanceMember;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Date;

@Singleton
public class EmailManager {

    private final EmailHandler emailHandler;

    @Inject
    public EmailManager(final jakarta.enterprise.inject.Instance<EmailHandler> emailHandlerInstance) {
        this.emailHandler = emailHandlerInstance.get();
    }

    public void sendInstanceExpiringNotification(final Instance instance, final Date expirationDate) {
        this.emailHandler.sendInstanceExpiringNotification(instance, expirationDate);
    }

    public void sendInstanceDeletedNotification(Instance instance, InstanceExpiration instanceExpiration) {
        this.emailHandler.sendInstanceDeletedNotification(instance, instanceExpiration);
    }

    public void sendInstanceLifetimeNotification(Instance instance) {
        this.emailHandler.sendInstanceLifetimeNotification(instance);
    }

    public void sendInstanceMemberAddedNotification(Instance instance, InstanceMember member) {
        this.emailHandler.sendInstanceMemberAddedNotification(instance, member);
    }

    public void sendInstanceCreatedNotification(final Instance instance) {
        this.emailHandler.sendInstanceCreatedNotification(instance);
    }

    public void sendInstanceExtensionRequestNotification(final Instance instance, final String comments, boolean autoAccepted) {
        this.emailHandler.sendInstanceExtensionRequestNotification(instance, comments, autoAccepted);
    }

    public void sendInstanceExtensionNotification(final Instance instance, boolean extensionGranted, final String handlerComments) {
        this.emailHandler.sendInstanceExtensionNotification(instance, extensionGranted, handlerComments);
    }

}
