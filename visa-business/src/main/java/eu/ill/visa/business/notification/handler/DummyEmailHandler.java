package eu.ill.visa.business.notification.handler;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceExpiration;
import eu.ill.visa.core.entity.InstanceMember;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@LookupIfProperty(name = "business.mailer.enabled", stringValue = "false")
@Singleton
public class DummyEmailHandler implements EmailHandler {

    private static final Logger logger = LoggerFactory.getLogger(DummyEmailHandler.class);

    @Inject
    public DummyEmailHandler() {
        logger.info("Email notification disabled");
    }

    public void sendInstanceExpiringNotification(final Instance instance, final Date expirationDate) {
    }

    public void sendInstanceDeletedNotification(Instance instance, InstanceExpiration instanceExpiration) {
    }

    public void sendInstanceLifetimeNotification(Instance instance) {
    }

    public void sendInstanceMemberAddedNotification(Instance instance, InstanceMember member) {
    }

    public void sendInstanceCreatedNotification(final Instance instance) {
    }

    public void sendInstanceExtensionRequestNotification(final Instance instance, final String comments, boolean autoAccepted) {
    }

    public void sendInstanceExtensionNotification(final Instance instance, boolean extensionGranted, final String handlerComments) {
    }

}
