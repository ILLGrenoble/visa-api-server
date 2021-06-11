package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.business.concurrent.InstanceActionFuture;
import eu.ill.visa.business.concurrent.InstanceActionManager;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import eu.ill.visa.persistence.repositories.InstanceCommandRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Transactional
@Singleton
public class InstanceCommandService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceCommandService.class);

    @Inject
    private InstanceCommandRepository repository;

    @Inject
    private InstanceActionManager instanceActionManager;



    public List<InstanceCommand> getAll() {
        return this.repository.getAll();
    }

    public List<InstanceCommand> getAllActive() {
        return this.repository.getAllActive();
    }

    public List<InstanceCommand> getAllPending() {
        return this.repository.getAllPending();
    }

    public InstanceCommand getById(Long id) {
        return this.repository.getById(id);
    }

    public List<InstanceCommand> getAllForUser(User user) {
        return this.repository.getAllForUser(user);
    }

    public List<InstanceCommand> getAllForInstance(Instance instance) {
        return this.repository.getAllForInstance(instance);
    }

    public InstanceCommand create(Instance instance, InstanceCommandType action) {
        InstanceCommand instanceCommand = new InstanceCommand(null, instance, action);
        this.save(instanceCommand);

        return instanceCommand;
    }

    public InstanceCommand create(User user, Instance instance, InstanceCommandType action) {
        InstanceCommand instanceCommand = new InstanceCommand(user, instance, action);
        this.save(instanceCommand);

        return instanceCommand;
    }

    public InstanceActionFuture execute(InstanceCommand instanceCommand) {
        return this.instanceActionManager.queue(instanceCommand);
    }

    public void cancel(InstanceCommand instanceCommand) {
        // Make sure we have the latest updated object
        instanceCommand = this.getById(instanceCommand.getId());

        this.instanceActionManager.cancel(instanceCommand);
    }

    public void save(InstanceCommand instanceCommand) {
        if (instanceCommand.getUser() != null) {
            logger.info("Saving command " + instanceCommand.getId() + " type " + instanceCommand.getActionType() + " from user " + (instanceCommand.getUser() != null ? instanceCommand.getUser().getFullName() : "null") + " on instance " + instanceCommand.getInstance().getId() + " state " + instanceCommand.getState());
            this.repository.save(instanceCommand);
        }
    }
}
