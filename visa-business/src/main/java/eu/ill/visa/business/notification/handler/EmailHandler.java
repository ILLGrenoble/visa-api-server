package eu.ill.visa.business.notification.handler;


import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceExpiration;
import eu.ill.visa.core.entity.InstanceMember;

import java.util.Date;

public interface EmailHandler {

    void sendInstanceExpiringNotification(final Instance instance, final Date expirationDate);
    void sendInstanceDeletedNotification(Instance instance, InstanceExpiration instanceExpiration);
    void sendInstanceLifetimeNotification(Instance instance);
    void sendInstanceMemberAddedNotification(Instance instance, InstanceMember member);
    void sendInstanceCreatedNotification(final Instance instance);
    void sendInstanceExtensionRequestNotification(final Instance instance, final String comments);
    void sendInstanceExtensionNotification(final Instance instance, boolean extensionGranted, final String handlerComments);
}
