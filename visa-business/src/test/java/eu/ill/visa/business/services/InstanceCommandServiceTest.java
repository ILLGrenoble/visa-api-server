package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceCommandState;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.persistence.repositories.UserRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestTransaction
public class InstanceCommandServiceTest {

    @Inject
    private InstanceCommandService instanceCommandService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private InstanceService instanceService;

    @Test
    @DisplayName("Get all instanceCommands")
    void testGetAll() {
        List<InstanceCommand> results = instanceCommandService.getAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Delete a instanceCommand")
    void testDelete() {
        List<InstanceCommand> initialCommands = instanceCommandService.getAllActive();
        assertEquals(2, initialCommands.size());

        InstanceCommand instanceCommand = instanceCommandService.getById(1000L);
        instanceCommandService.cancel(instanceCommand);

        List<InstanceCommand> finalCommands = instanceCommandService.getAllActive();
        assertEquals(1, finalCommands.size());
    }

    @Test
    @DisplayName("Get all instanceCommands for a user")
    void testGetAllForAUser() {
        User user = userRepository.getById("1");
        assertNotNull(user);

        List<InstanceCommand> results = instanceCommandService.getAllForUser(user);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Get all instanceCommands for a instance")
    void testGetAllForAInstance() {
        Instance instance = instanceService.getById(1000L);
        assertNotNull(instance);

        List<InstanceCommand> results = instanceCommandService.getAllForInstance(instance);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Create instanceCommand")
    void testCreate() {
        User user = userRepository.getById("1");
        assertNotNull(user);

        Instance instance = instanceService.getById(1000L);
        assertNotNull(instance);

        InstanceCommand instanceCommand = instanceCommandService.create(user, instance, InstanceCommandType.START);

        Long instanceCommandId = instanceCommand.getId();
        assertNotNull(instanceCommandId);

        InstanceCommand persistedInstanceCommand = instanceCommandService.getById(instanceCommandId);
        assertEquals(instanceCommand, persistedInstanceCommand);
    }

    @Test
    @DisplayName("Modify instanceCommand")
    void testModify() {
        User user = userRepository.getById("1");
        assertNotNull(user);

        Instance instance = instanceService.getById(1000L);
        assertNotNull(instance);

        InstanceCommand instanceCommand = instanceCommandService.create(user, instance, InstanceCommandType.START);

        Long instanceCommandId = instanceCommand.getId();
        assertNotNull(instanceCommandId);

        instanceCommand.setState(InstanceCommandState.RUNNING);
        instanceCommandService.save(instanceCommand);

        InstanceCommand persistedInstanceCommand = instanceCommandService.getById(instanceCommandId);
        assertEquals(InstanceCommandState.RUNNING, persistedInstanceCommand.getState());
    }
}
