package eu.ill.visa.business.concurrent.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceSession;
import eu.ill.visa.core.domain.InstanceSessionMember;

import java.util.List;

@Singleton
public class InstanceActionServiceProvider {

    private final NotificationService notificationService;
    private final InstanceService instanceService;

    private final InstanceSessionService instanceSessionService;
    private final InstanceCommandService instanceCommandService;
    private final SecurityGroupService securityGroupService;
    private final InstrumentService instrumentService;
    private final SignatureService signatureService;

    private final CloudClientGateway cloudClientGateway;


    @Inject
    public InstanceActionServiceProvider(final InstanceService instanceService,
                                         final InstanceSessionService instanceSessionService,
                                         final InstanceCommandService instanceCommandService,
                                         final SecurityGroupService securityGroupService,
                                         final InstrumentService instrumentService,
                                         final CloudClientGateway cloudClientGateway,
                                         final NotificationService notificationService,
                                         final SignatureService signatureService) {
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.instanceCommandService = instanceCommandService;
        this.securityGroupService = securityGroupService;
        this.instrumentService = instrumentService;
        this.cloudClientGateway = cloudClientGateway;
        this.notificationService = notificationService;
        this.signatureService = signatureService;
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
        // TODO CloudClient: select specific cloud client
        CloudClient cloudClient = this.cloudClientGateway.getDefaultCloudClient();
        return cloudClient;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public SignatureService getSignatureService() {
        return signatureService;
    }
}
