package eu.ill.visa.business.concurrent.actions;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.business.notification.EmailManager;
import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class InstanceActionServiceProvider {

    private final EmailManager emailManager;
    private final InstanceService instanceService;

    private final InstanceSessionService instanceSessionService;
    private final InstanceSessionMemberService instanceSessionMemberService;
    private final InstanceCommandService instanceCommandService;
    private final SecurityGroupService securityGroupService;
    private final InstrumentService instrumentService;
    private final SignatureService signatureService;
    private final CloudClientService cloudClientService;
    private final PortService portService;
    private final ImageService imageService;
    private final EventDispatcher eventDispatcher;


    @Inject
    public InstanceActionServiceProvider(final InstanceService instanceService,
                                         final InstanceSessionService instanceSessionService,
                                         final InstanceSessionMemberService instanceSessionMemberService,
                                         final InstanceCommandService instanceCommandService,
                                         final SecurityGroupService securityGroupService,
                                         final InstrumentService instrumentService,
                                         final CloudClientService cloudClientService,
                                         final EmailManager emailManager,
                                         final SignatureService signatureService,
                                         final PortService portService,
                                         final ImageService imageService,
                                         final EventDispatcher eventDispatcher) {
        this.instanceService = instanceService;
        this.instanceSessionService = instanceSessionService;
        this.instanceSessionMemberService = instanceSessionMemberService;
        this.instanceCommandService = instanceCommandService;
        this.securityGroupService = securityGroupService;
        this.instrumentService = instrumentService;
        this.cloudClientService = cloudClientService;
        this.emailManager = emailManager;
        this.signatureService = signatureService;
        this.portService = portService;
        this.imageService = imageService;
        this.eventDispatcher = eventDispatcher;
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
            this.instanceSessionService.updatePartial(session);
        }

        final List<InstanceSessionMemberPartial> sessionMembers = this.instanceSessionMemberService.getAllPartialsByInstanceId(instance.getId());
        for (InstanceSessionMemberPartial member : sessionMembers) {
            this.instanceSessionMemberService.deactivateSessionMember(member);
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

    public CloudClient getCloudClient(Long cloudClientId) {
        return this.cloudClientService.getCloudClient(cloudClientId);
    }

    public EmailManager getEmailManager() {
        return emailManager;
    }

    public SignatureService getSignatureService() {
        return signatureService;
    }

    public PortService getPortService() {
        return portService;
    }

    public ImageService getImageService() {
        return imageService;
    }

    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }
}
