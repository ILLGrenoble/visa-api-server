package eu.ill.visa.business.concurrent;

import eu.ill.visa.business.concurrent.actions.InstanceActionServiceProvider;
import eu.ill.visa.business.profiles.ConcurrencyTestProfile;
import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.PlanService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.Plan;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceCommandState;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.persistence.repositories.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(ConcurrencyTestProfile.class)
public class InstanceActionManagerTest {

    @Inject
    private InstanceActionManager instanceActionManager;

    @Inject
    private InstanceCommandService instanceCommandService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PlanService planService;

    @Inject
    private InstanceService instanceService;

    @Inject
    private InstanceActionServiceProvider serviceProvider;


    private Instance createInstance() {
        Plan plan = planService.getById(1000L);

        Instance.Builder instanceBuilder = Instance.builder()
            .plan(plan)
            .name("Test instance")
            .comments("This is an instance")
            .keyboardLayout("fr-fr-azerty")
            .screenWidth(1280)
            .screenHeight(1024);
        Instance instance = instanceService.create(instanceBuilder);
        Long instanceId = instance.getId();
        assertNotNull(instanceId);
        assertEquals(InstanceState.BUILDING, instance.getState());

        return instance;
    }

    @Test
    @DisplayName("Create instanceCommand")
    void testCreate() {
        User user = userRepository.getById("1");
        assertNotNull(user);

        Instance instance = this.createInstance();
        assertNotNull(instance);

        InstanceCommand instanceCommand = instanceCommandService.create(user, instance, InstanceCommandType.START);

        Long instanceCommandId = instanceCommand.getId();
        assertNotNull(instanceCommandId);

        DummyInstanceAction dummyInstanceAction = new DummyInstanceAction(serviceProvider, instanceCommand, 2000);
        this.instanceActionManager.queue(dummyInstanceAction);

        instanceCommand = instanceCommandService.getById(instanceCommandId);
        assertNotEquals(InstanceCommandState.PENDING, instanceCommand.getState());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instanceCommand = instanceCommandService.getById(instanceCommandId);
        assertEquals(InstanceCommandState.RUNNING, instanceCommand.getState());

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instanceCommand = instanceCommandService.getById(instanceCommandId);
        Instance persistedInstance = instanceService.getById(instanceCommand.getInstance().getId());
        assertEquals(InstanceCommandState.TERMINATED, instanceCommand.getState());
        assertEquals(InstanceState.ACTIVE, persistedInstance.getState());
    }

    @Test
    @DisplayName("Test returned future object")
    void testReturnedFuture() throws ExecutionException, InterruptedException {
        User user = userRepository.getById("1");
        assertNotNull(user);

        Instance instance = this.createInstance();

        InstanceCommand instanceCommand = instanceCommandService.create(user, instance, InstanceCommandType.START);

        Long instanceCommandId = instanceCommand.getId();
        assertNotNull(instanceCommandId);

        long startTime = (new Date()).getTime();
        DummyInstanceAction dummyInstanceAction = new DummyInstanceAction(serviceProvider, instanceCommand, 2000);
        InstanceActionFuture actionFuture = this.instanceActionManager.queue(dummyInstanceAction);

        // Wait for execution to terminate
        Instance futureInstance = actionFuture.getFutureInstance();

        long endTime = (new Date()).getTime();
        assertTrue(endTime > startTime + 2000);

        instanceCommand = instanceCommandService.getById(instanceCommandId);
        assertEquals(InstanceCommandState.TERMINATED, instanceCommand.getState());
        assertEquals(InstanceState.ACTIVE, futureInstance.getState());
    }

    @Test
    @DisplayName("Test returned future object with exception")
    void testReturnedFutureWithException() throws InterruptedException {
        User user = userRepository.getById("1");
        assertNotNull(user);

        Instance instance = this.createInstance();
        assertNotNull(instance);

        InstanceCommand instanceCommand = instanceCommandService.create(user, instance, InstanceCommandType.START);

        Long instanceCommandId = instanceCommand.getId();
        assertNotNull(instanceCommandId);

        DummyExceptionThrowingInstanceAction action = new DummyExceptionThrowingInstanceAction(serviceProvider, instanceCommand, 2000);
        InstanceActionFuture actionFuture = this.instanceActionManager.queue(action);

        // Wait for execution to terminate
        boolean gotException = false;
        try {
            actionFuture.getFutureInstance();

        } catch (ExecutionException e) {
            gotException = true;
        }

        assertTrue(gotException);
        instanceCommand = instanceCommandService.getById(instanceCommandId);
        assertEquals(InstanceCommandState.FAILED, instanceCommand.getState());
    }


    @Test
    @DisplayName("Create queued instanceCommand")
    void testQueue() {
        User user = userRepository.getById("1");
        assertNotNull(user);

        Instance instance = this.createInstance();
        assertNotNull(instance);

        InstanceCommand instanceCommand1 = instanceCommandService.create(user, instance, InstanceCommandType.START);
        InstanceCommand instanceCommand2 = instanceCommandService.create(user, instance, InstanceCommandType.SHUTDOWN);

        Long instanceCommand1Id = instanceCommand1.getId();
        Long instanceCommand2Id = instanceCommand2.getId();
        assertNotNull(instanceCommand1Id);
        assertNotNull(instanceCommand2Id);

        DummyInstanceAction dummyInstanceAction1 = new DummyInstanceAction(serviceProvider, instanceCommand1, 2000);
        DummyInstanceAction dummyInstanceAction2 = new DummyInstanceAction(serviceProvider, instanceCommand2, 2000);
        this.instanceActionManager.queue(dummyInstanceAction1);
        this.instanceActionManager.queue(dummyInstanceAction2);

        instanceCommand1 = instanceCommandService.getById(instanceCommand1Id);
        instanceCommand2 = instanceCommandService.getById(instanceCommand2Id);
        assertNotEquals(InstanceCommandState.PENDING, instanceCommand1.getState());
        assertNotEquals(InstanceCommandState.PENDING, instanceCommand2.getState());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instanceCommand1 = instanceCommandService.getById(instanceCommand1Id);
        instanceCommand2 = instanceCommandService.getById(instanceCommand2Id);
        assertEquals(InstanceCommandState.RUNNING, instanceCommand1.getState());
        assertEquals(InstanceCommandState.QUEUED, instanceCommand2.getState());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instanceCommand1 = instanceCommandService.getById(instanceCommand1Id);
        instanceCommand2 = instanceCommandService.getById(instanceCommand2Id);
        assertEquals(InstanceCommandState.TERMINATED, instanceCommand1.getState());
        assertEquals(InstanceCommandState.RUNNING, instanceCommand2.getState());

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instanceCommand2 = instanceCommandService.getById(instanceCommand2Id);
        assertEquals(InstanceCommandState.TERMINATED, instanceCommand2.getState());
    }


    @Test
    @DisplayName("Cancel instance command")
    void testCancel() {
        User user = userRepository.getById("1");
        assertNotNull(user);

        Instance instance = this.createInstance();
        assertNotNull(instance);

        InstanceCommand instanceCommand = instanceCommandService.create(user, instance, InstanceCommandType.START);

        Long instanceCommandId = instanceCommand.getId();
        assertNotNull(instanceCommandId);

        DummyInstanceAction dummyInstanceAction = new DummyInstanceAction(serviceProvider, instanceCommand, 2000);
        this.instanceActionManager.queue(dummyInstanceAction);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.instanceCommandService.cancel(instanceCommand);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        instanceCommand = instanceCommandService.getById(instanceCommandId);
        assertEquals(InstanceCommandState.CANCELLED, instanceCommand.getState());
    }

}
