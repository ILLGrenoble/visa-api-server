package eu.ill.visa.business.notification.handler;


import eu.ill.visa.core.entity.*;

import java.util.Date;

public interface EmailHandler {

    void sendInstanceExpiringNotification(final Instance instance, final Date expirationDate);
    void sendInstanceDeletedNotification(Instance instance, InstanceExpiration instanceExpiration);
    void sendInstanceLifetimeNotification(Instance instance);
    void sendInstanceMemberAddedNotification(Instance instance, InstanceMember member);
    void sendInstanceCreatedNotification(final Instance instance);
    void sendInstanceExtensionRequestNotification(final Instance instance, final String comments, boolean autoAccepted);
    void sendInstanceExtensionNotification(final Instance instance, boolean extensionGranted, final String handlerComments);
    void sendBookingRequestCreatedToAdmin(BookingRequest bookingRequest, boolean isUpdate);
    void sendBookingRequestCreatedToOwner(BookingRequest bookingRequest, boolean isUpdate);
    void sendBookingRequestValidated(BookingRequest bookingRequest);
    void sendBookingRequestTokenNotification(BookingRequest bookingRequest, User tokenOwner);
}
