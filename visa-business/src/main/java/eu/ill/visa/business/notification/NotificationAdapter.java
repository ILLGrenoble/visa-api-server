package eu.ill.visa.business.notification;

import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceExpiration;
import eu.ill.visa.core.domain.InstanceMember;

import java.util.Date;

public interface NotificationAdapter {
    void sendInstanceCreatedNotification(Instance instance);

    void sendInstanceExpiringNotification(Instance instance, Date expirationDate);

    void sendInstanceDeletedNotification(Instance instance, InstanceExpiration instanceExpiration);

    void sendInstanceLifetimeNotification(Instance instance);

    void sendInstanceMemberAddedNotification(Instance instance, InstanceMember member);
}
