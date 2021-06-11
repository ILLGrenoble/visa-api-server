package eu.ill.visa.business.notification;

import com.google.inject.Inject;
import eu.ill.visa.business.services.BusinessExtension;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceExpiration;
import eu.ill.visa.core.domain.InstanceMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.Optional;

import static eu.ill.visa.core.domain.enumerations.InstanceMemberRole.USER;
import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@ExtendWith(BusinessExtension.class)
class EmailNotificationAdapterTest {

    @Inject
    public InstanceService instanceService;

    private EmailNotificationAdapter emailNotificationAdapter;

    @BeforeEach()
    public void createNotificationAdapter() {
        final String host = getenv("VISA_NOTIFICATION_EMAIL_ADAPTER_HOST");
        final String port = getenv("VISA_NOTIFICATION_EMAIL_ADAPTER_PORT");
        final String fromEmailAddress = getenv("VISA_NOTIFICATION_EMAIL_ADAPTER_FROM_EMAIL_ADDRESS");
        final String adminEmailAddress = getenv("VISA_NOTIFICATION_EMAIL_ADAPTER_ADMIN_EMAIL_ADDRESS");
        final String userMaxInactivityDurationHours = getenv("VISA_INSTANCE_USER_MAX_INACTIVITY_DURATION_HOURS");
        final String staffMaxInactivityDurationHours = getenv("VISA_INSTANCE_STAFF_MAX_INACTIVITY_DURATION_HOURS");
        final String userMaxLifetimeDurationHours = getenv("VISA_INSTANCE_USER_MAX_LIFETIME_DURATION_HOURS");
        final String staffMaxLifetimeDurationHours = getenv("VISA_INSTANCE_STAFF_MAX_LIFETIME_DURATION_HOURS");
        final String emailTemplatesDirectory = getenv("VISA_NOTIFICATION_EMAIL_ADAPTER_TEMPLATES_DIRECTORY");
        final String rootURL = getenv("VISA_ROOT_URL");
        if (host != null && port != null && fromEmailAddress != null && adminEmailAddress != null && userMaxInactivityDurationHours != null && staffMaxInactivityDurationHours != null && userMaxLifetimeDurationHours != null  && staffMaxLifetimeDurationHours != null && rootURL != null) {
            this.emailNotificationAdapter = new EmailNotificationAdapter(host, parseInt(port), fromEmailAddress, null, adminEmailAddress, emailTemplatesDirectory,rootURL, parseInt(userMaxInactivityDurationHours), parseInt(staffMaxInactivityDurationHours), parseInt(userMaxLifetimeDurationHours), parseInt(staffMaxLifetimeDurationHours));
        }
    }

    @Test
    @DisplayName("it should send an expiration email")
    void it_should_send_an_expiration_email() {
        final Instance instance = instanceService.getById(1000L);
        final Date now = new Date();
        instance.setCreatedAt(addDays(now, -2));
        instance.setTerminationDate(addDays(now, 5));
        if (this.emailNotificationAdapter != null) {
            this.emailNotificationAdapter.sendInstanceExpiringNotification(instance, addDays(now, 1));
        }
    }

    @Test
    @DisplayName("it should send a deleted email because the instance has reached its maximum life time")
    void it_should_send_a_deleted_email_because_reached_max_life_time() {
        final Instance instance = instanceService.getById(1000L);
        final InstanceExpiration expiration = new InstanceExpiration();
        expiration.setExpirationDate(instance.getTerminationDate());
        expiration.setInstance(instance);
        if (this.emailNotificationAdapter != null) {
            this.emailNotificationAdapter.sendInstanceDeletedNotification(instance, expiration);
        }
    }

    @Test
    @DisplayName("it should send a deleted email because of inactivity")
    void it_should_a_deleted_email_because_of_inactivity() {
        final Instance instance = instanceService.getById(1000L);
        final InstanceExpiration expiration = new InstanceExpiration();
        expiration.setExpirationDate(addDays(instance.getTerminationDate(), -1));
        expiration.setInstance(instance);
        if (this.emailNotificationAdapter != null) {
            this.emailNotificationAdapter.sendInstanceDeletedNotification(instance, expiration);
        }
    }

    @Test
    @DisplayName("it should send an email because lifetime is almost reached")
    void it_should_send_an_email_because_lifetime_is_almost_reached() {
        final Instance instance = instanceService.getById(1000L);
        if (this.emailNotificationAdapter != null) {
            this.emailNotificationAdapter.sendInstanceLifetimeNotification(instance);
        }
    }

    @Test
    @DisplayName("it should send an email because a member has been added to an instance")
    void it_should_send_an_email_because_a_member_has_been_added_to_an_instance() {
        final Instance instance = instanceService.getById(1000L);
        final Optional<InstanceMember> member = instance.getMembers().stream()
            .filter(object -> object.isRole(USER))
            .findFirst();
        if (member.isPresent()) {
            if (this.emailNotificationAdapter != null) {
                this.emailNotificationAdapter.sendInstanceMemberAddedNotification(instance, member.get());
            }
        }
    }

}
