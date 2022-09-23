package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.InstanceSessionMemberRepository;
import eu.ill.visa.persistence.repositories.InstanceSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
@Singleton
public class InstanceSessionService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSessionService.class);

    private InstanceSessionRepository repository;
    private InstanceSessionMemberRepository instanceSessionMemberRepository;
    private InstanceJupyterSessionService instanceJupyterSessionService;

    @Inject
    public InstanceSessionService(InstanceSessionRepository repository, InstanceSessionMemberRepository instanceSessionMemberRepository, InstanceJupyterSessionService instanceJupyterSessionService) {
        this.repository = repository;
        this.instanceSessionMemberRepository = instanceSessionMemberRepository;
        this.instanceJupyterSessionService = instanceJupyterSessionService;
    }

    public List<InstanceSession> getAll() {
        return this.repository.getAll();
    }

    public InstanceSession getById(@NotNull Long id) {
        return this.repository.getById(id);
    }

    public InstanceSession create(@NotNull Instance instance, String connectionId) {
        InstanceSession session = new InstanceSession(instance, connectionId);

        this.save(session);

        return session;
    }

    public InstanceSession getByInstance(@NotNull Instance instance) {
        return this.repository.getByInstance(instance);
    }

    public List<InstanceSession> getAllByInstance(@NotNull Instance instance) {
        return this.repository.getAllByInstance(instance);
    }

    public void save(@NotNull InstanceSession instanceSession) {
        this.repository.save(instanceSession);
    }

    public void addInstanceSessionMember(@NotNull InstanceSession instanceSession, UUID sessionId, User user, String role) {
        InstanceSessionMember sessionMember = new InstanceSessionMember(instanceSession, sessionId.toString(), user, role);
        sessionMember.setActive(true);

        this.instanceSessionMemberRepository.save(sessionMember);
    }

    public void removeInstanceSessionMember(@NotNull InstanceSession instanceSession, UUID sessionId) {
        InstanceSessionMember sessionMember = this.instanceSessionMemberRepository.getSessionMember(instanceSession, sessionId.toString());
        if (sessionMember != null) {
            sessionMember.setActive(false);
            this.instanceSessionMemberRepository.save(sessionMember);

            List<InstanceSessionMember> members = this.instanceSessionMemberRepository.getAllSessionMembers(instanceSession);

            if (members.size() == 0) {
                logger.info("Session for Instance with Id: {} is no longer current as it has no connected members", instanceSession.getInstance().getId());
                instanceSession.setCurrent(false);
            }
            this.repository.save(instanceSession);
        } else {
            logger.warn("Got a null session member (session {}) for instance {}", sessionId.toString(), instanceSession.getInstance().getId());
        }
    }

    public List<InstanceSessionMember> getAllSessionMembers(@NotNull InstanceSession instanceSession) {
        return this.instanceSessionMemberRepository.getAllSessionMembers(instanceSession);
    }

    public List<InstanceSessionMember> getAllHistorySessionMembers(final Instance instance) {
        return this.instanceSessionMemberRepository.getAllHistorySessionMembers(instance);

    }

    public List<InstanceSessionMember> getAllSessionMembers(@NotNull Instance instance) {
        return this.instanceSessionMemberRepository.getAllSessionMembers(instance);
    }

    public InstanceSessionMember getSessionMemberBySessionId(UUID sessionId) {
        return this.instanceSessionMemberRepository.getBySessionId(sessionId.toString());
    }

    public void saveInstanceSessionMember(InstanceSessionMember instanceSessionMember) {
        this.instanceSessionMemberRepository.save(instanceSessionMember);
    }

    public void cleanupSession() {
        List<InstanceSession> activeSessions = this.repository.getAll();
        for (InstanceSession instanceSession : activeSessions) {
            instanceSession.setCurrent(false);
            this.repository.save(instanceSession);
        }

        List<InstanceSessionMember> activeSessionMembers = this.instanceSessionMemberRepository.getAll();
        for (InstanceSessionMember instanceSessionMember : activeSessionMembers) {
            instanceSessionMember.setActive(false);
            this.instanceSessionMemberRepository.save(instanceSessionMember);
        }

        // Do the cleanup here... not ideal but at least it is centralised
        this.instanceJupyterSessionService.cleanupSession();
    }

    public boolean canConnectWhileOwnerAway(Instance instance, User user) {
        InstanceMember owner = instance.getOwner();
        if (owner.getUser().getId().equals(user.getId())) {
            // The user is the owner
            return true;
        }

        // Allow INSTRUMENT_SCIENTIST user to connect to standard user instances (if IR is responsible for instrument associated to experiments of the instance)
        boolean ownerIsExternalUser = !owner.getUser().hasRole(Role.STAFF_ROLE);
        boolean userIsInstrumentScientist = user.hasRole(Role.INSTRUMENT_SCIENTIST_ROLE);
        boolean userIsMember = instance.isMember(user);
        if (ownerIsExternalUser && userIsInstrumentScientist) {
            // Get IRs for the instance experiments
            List<String> instrumentScientistIds = instance.getExperiments().stream()
                .map(experiment -> experiment.getInstrument().getScientists())
                .flatMap(List::stream)
                .map(User::getId)
                .distinct()
                .collect(Collectors.toList());

            // Return true if the user is an instrument scientist for the associated instruments of the instance
            return instrumentScientistIds.contains(user.getId());

        } else if (!ownerIsExternalUser && userIsInstrumentScientist) {
            // If owner is staff then the instrument scientist can only connect if there is an active experiment
            List<Experiment> activeExperiments = instance.getActiveExperiments();
            List<String> activeExperimentInstrumentScientistIds = activeExperiments.stream()
                .map(experiment -> experiment.getInstrument().getScientists())
                .flatMap(List::stream)
                .map(User::getId)
                .distinct()
                .collect(Collectors.toList());

            // Return true if the user is an instrument scientist for the associated instruments of the instance
            return activeExperimentInstrumentScientistIds.contains(user.getId());
        }

        // Allow ADMIN user to connect to any standard user instances
        boolean userIsAdmin = user.hasRole(Role.ADMIN_ROLE);
        return ownerIsExternalUser && userIsAdmin;
    }
}
