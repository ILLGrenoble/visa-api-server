package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.InstanceJupyterSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class InstanceJupyterSessionService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceJupyterSessionService.class);

    private InstanceJupyterSessionRepository repository;

    @Inject
    public InstanceJupyterSessionService(InstanceJupyterSessionRepository repository) {
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

    public List<InstanceJupyterSession> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public Long countAll(QueryFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, QueryFilter::new));
    }

    public Long countAllInstances() {
        return repository.countAllInstances();
    }
}
