package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceJupyterSession;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.persistence.repositories.InstanceJupyterSessionRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Transactional
@Singleton
public class InstanceJupyterSessionService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceJupyterSessionService.class);

    private final InstanceJupyterSessionRepository repository;

    @Inject
    public InstanceJupyterSessionService(final InstanceJupyterSessionRepository repository) {
        this.repository = repository;
    }

    public List<InstanceJupyterSession> getAll() {
        return this.repository.getAll();
    }

    public InstanceJupyterSession create(@NotNull Instance instance, @NotNull User user, @NotNull String kernelId, @NotNull String sessionId) {
        // Destroy any previous ones with the same IDs (rare/should never exist)
        this.destroy(instance, kernelId, sessionId);

        // Create new session
        InstanceJupyterSession session = new InstanceJupyterSession(instance, user, kernelId, sessionId);

        this.save(session);

        return session;
    }

    public void destroy(@NotNull Instance instance, @NotNull String kernelId, @NotNull String sessionId) {
        List<InstanceJupyterSession> sessions = this.getAllByInstanceKernelSession(instance, kernelId, sessionId);
        for (InstanceJupyterSession session : sessions) {
            session.setActive(false);

            this.save(session);
        }
    }

    public List<InstanceJupyterSession> getAllByInstance(@NotNull Instance instance) {
        return this.repository.getAllByInstance(instance);
    }

    public List<InstanceJupyterSession> getAllByInstanceKernelSession(@NotNull Instance instance, @NotNull String kernelId, @NotNull String sessionId) {
        return this.repository.getAllByInstanceKernelSession(instance, kernelId, sessionId);
    }

    public void save(@NotNull InstanceJupyterSession instanceJupyterSession) {
        this.repository.save(instanceJupyterSession);
    }


    public void cleanupSession() {
        List<InstanceJupyterSession> activeSessions = this.repository.getAll();
        for (InstanceJupyterSession instanceJupyterSession : activeSessions) {
            instanceJupyterSession.setActive(false);
            this.repository.save(instanceJupyterSession);
        }
    }

    public List<InstanceJupyterSession> getAll(Pagination pagination) {
        return this.repository.getAll(pagination);
    }

    public Long countAll() {
        return repository.countAll();
    }

    public Long countAllInstances() {
        return repository.countAllInstances();
    }
}
