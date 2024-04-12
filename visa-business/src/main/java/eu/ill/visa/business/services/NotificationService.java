package eu.ill.visa.business.services;

import jakarta.inject.Singleton;
import eu.ill.visa.business.notification.NotificationAdapter;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceExpiration;
import eu.ill.visa.core.domain.InstanceMember;

import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Singleton
public class NotificationService {

    private final List<NotificationAdapter> adapters;

    public NotificationService(final List<NotificationAdapter> adapters) {
        this.adapters = requireNonNull(adapters, "adapters cannot be null");
    }

    public void sendInstanceCreationEmail(final Instance instance) {
        adapters.forEach(adapter -> {
            adapter.sendInstanceCreatedNotification(requireNonNull(instance, "instance cannot be null"));
        });
    }

    public void sendInstanceExpiringNotification(Instance instance, Date expirationDate) {
        adapters.forEach(adapter -> {
            adapter.sendInstanceExpiringNotification(
                requireNonNull(instance, "instance cannot be null"),
                requireNonNull(expirationDate, "expirationDate cannot be null")
            );
        });
    }

    public void sendInstanceDeletedNotification(Instance instance, InstanceExpiration instanceExpiration) {
        adapters.forEach(adapter -> {
            adapter.sendInstanceDeletedNotification(
                requireNonNull(instance, "instance cannot be null"),
                requireNonNull(instanceExpiration, "instanceExpiration cannot be null")
            );
        });
    }

    public void sendInstanceLifetimeNotification(Instance instance) {
        adapters.forEach(adapter -> {
            adapter.sendInstanceLifetimeNotification(
                requireNonNull(instance, "instance cannot be null")
            );
        });
    }

    public void sendInstanceMemberAddedNotification(Instance instance, InstanceMember member) {
        adapters.forEach(adapter -> {
            adapter.sendInstanceMemberAddedNotification(
                requireNonNull(instance, "instance cannot be null"),
                requireNonNull(member, "member cannot be null")
            );
        });
    }

    public void sendInstanceExtensionRequestNotification(Instance instance, String comments) {
        adapters.forEach(adapter -> {
            adapter.sendInstanceExtensionRequestNotification(
                requireNonNull(instance, "instance cannot be null"),
                requireNonNull(comments, "comments cannot be null")
            );
        });
    }

    public void sendInstanceExtensionNotification(Instance instance, boolean extensionGranted, String handlerComments) {
        adapters.forEach(adapter -> {
            adapter.sendInstanceExtensionNotification(
                requireNonNull(instance, "instance cannot be null"),
                extensionGranted,
                handlerComments
            );
        });
    }

}
