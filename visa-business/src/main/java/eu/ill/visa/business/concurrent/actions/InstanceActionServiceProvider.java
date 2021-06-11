package eu.ill.visa.business.concurrent.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceSession;
import eu.ill.visa.core.domain.InstanceSessionMember;

import java.util.List;

@Singleton
public class InstanceActionServiceProvider {

    private NotificationService notificationService;
    private InstanceService          instanceService;

    private InstanceSessionService instanceSessionService;
    private InstanceCommandService instanceCommandService;
    private SecurityGroupService securityGroupService;
    private InstrumentService instrumentService;

    private CloudClient cloudClient;


    @Inject
    public InstanceActionServiceProvider(InstanceService instanceService,
                                         InstanceSessionService instanceSessionService,
                                         InstanceCommandService instanceCommandService,
                                         SecurityGroupService securityGroupService,
                                         InstrumentService instrumentService,
                                         CloudClient cloudClient,
                                         NotificationService notificationService) {
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.instanceCommandService = instanceCommandService;
        this.securityGroupService = securityGroupService;
        this.instrumentService = instrumentService;
        this.cloudClient = cloudClient;
        this.notificationService = notificationService;
    }

    /**
     * Remove instance session for a given instance
     *
     * @param instance the instance
     */
    public void clearSessionsForInstance(Instance instance) {
        final List<InstanceSession> sessions = this.instanceSessionService.getAllByInstance(instance);
        for (InstanceSession session : sessions) {
            session.setCurrent(false);
            this.instanceSessionService.save(session);
        }

        final List<InstanceSessionMember> sessionMembers = this.instanceSessionService.getAllSessionMembers(instance);
        for (InstanceSessionMember member : sessionMembers) {
            member.setActive(false);
            this.instanceSessionService.saveInstanceSessionMember(member);
        }
    }

    public InstanceService getInstanceService() {
        return instanceService;
    }

    public InstanceCommandService getInstanceCommandService() {
        return instanceCommandService;
    }

    public SecurityGroupService getSecurityGroupService() {
        return securityGroupService;
    }

    public InstrumentService getInstrumentService() {
        return instrumentService;
    }

    public CloudClient getCloudClient() {
        return cloudClient;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
}
