package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.core.entity.*;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static eu.ill.visa.core.entity.enumerations.InstanceMemberRole.OWNER;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
public class InstanceServiceTest {

    @Inject
    private InstanceService instanceService;

    @Inject
    private PlanService planService;

    @Inject
    private UserService userService;

    @Inject
    private ExperimentService experimentService;

    @Test
    @DisplayName("Get all instances")
    void testGetAll() {
        List<Instance> results = instanceService.getAll();
        assertEquals(11, results.size());
    }

    @Test
    @DisplayName("Count all instances")
    void testCountAll() {
        Long count = instanceService.countAll();
        assertEquals(11, count);
    }

    @Test
    @DisplayName("Count all instances for a given state")
    void testCountAllForState() {
        assertEquals(11, instanceService.countAllForState(InstanceState.ACTIVE));
        assertEquals(0, instanceService.countAllForState(InstanceState.BUILDING));
    }

    @Test
    @DisplayName("Get all instances for given states")
    void testGetAllWithStates() {
        final List<InstanceState> states = Arrays.asList(InstanceState.BUILDING, InstanceState.ACTIVE, InstanceState.DELETED);
        assertEquals(11, instanceService.getAllWithStates(states).size());
    }

    @Test
    @DisplayName("Get a instance by a known id")
    void testGetById() {
        Long id = 1000L;
        Instance instance = instanceService.getById(id);
        assertEquals(id, instance.getId());
        assertEquals("Instance 1", instance.getName());
    }

    @Test
    @DisplayName("Get a instance by an unknown id")
    void testGetByUnknownId() {
        Instance instance = instanceService.getById(1000000L);
        assertNull(instance);
    }

    @Test
    @DisplayName("Verify instance is coherent")
    void verifyInstanceCoherent() {
        Instance instance = instanceService.getById(1000L);

        Plan plan = instance.getPlan();
        assertEquals(1001L, plan.getId());

        Image image = plan.getImage();
        assertEquals(1001L, image.getId());

        Flavour flavour = plan.getFlavour();
        assertEquals(1001L, flavour.getId());

        assertEquals(InstanceState.ACTIVE, instance.getState());
    }

    @Test
    @DisplayName("Create an instance")
    void createInstance() {
        Plan plan = planService.getById(1000L);

        Instance.Builder instanceBuilder = Instance.builder()
            .plan(plan)
            .name("Test instance")
            .comments("This is an instance")
            .username("hall")
            .screenWidth(1280)
            .screenHeight(1024)
            .keyboardLayout("fr-fr-azerty")
            .attributes(singletonList(new InstanceAttribute("home.directory", "/home/acme")));
        Instance instance = instanceService.create(instanceBuilder);
        Long instanceId = instance.getId();

        assertNotNull(instanceId);

        Instance persistedInstance = instanceService.getById(instanceId);
        assertEquals(instance, persistedInstance);
    }

    @Test
    @DisplayName("Create a instance with a member")
    void createInstanceWithAMember() {
        Plan plan = planService.getById(1000L);

        Instance.Builder instanceBuilder = Instance.builder()
            .plan(plan)
            .name("Test instance")
            .comments("This is an instance")
            .screenWidth(1280)
            .screenHeight(1024)
            .keyboardLayout("en-gb-qwerty");
        Instance instance = instanceService.create(instanceBuilder);
        Long instanceId = instance.getId();

        User user = userService.getById("1");
        instance.createMember(user, OWNER);
        instanceService.save(instance);

        assertNotNull(instanceId);

        Instance persistedInstance = instanceService.getById(instanceId);
        assertEquals(instance, persistedInstance);
        assertEquals(1, persistedInstance.getMembers().size());
        assertEquals("1", persistedInstance.getMembers().get(0).getUser().getId());
        assertEquals(OWNER, persistedInstance.getMembers().get(0).getRole());
        assertEquals("en-gb-qwerty", persistedInstance.getKeyboardLayout());
    }

    @Test
    @DisplayName("Create an instance with a member using builder")
    void createInstanceWithAMemberWithBuilder() {
        Plan plan = planService.getById(1000L);

        User user = userService.getById("1");

        Instance.Builder instanceBuilder = Instance.builder()
            .member(user, OWNER)
            .plan(plan)
            .name("Test instance")
            .comments("This is an instance")
            .screenWidth(1280)
            .screenHeight(1024)
            .keyboardLayout("en-gb-qwerty");
        Instance instance = instanceService.create(instanceBuilder);
        Long instanceId = instance.getId();

        assertNotNull(instanceId);

        Instance persistedInstance = instanceService.getById(instanceId);
        assertEquals(instance, persistedInstance);
        assertEquals(1, persistedInstance.getMembers().size());
        assertEquals("1", persistedInstance.getMembers().get(0).getUser().getId());
        assertEquals(OWNER, persistedInstance.getMembers().get(0).getRole());

    }
    @Test
    @DisplayName("Add a member to an instance")
    void addMemberToInstance() {
        Instance instance = instanceService.getById(1002L);
        assertNotNull(instance);

        User user = userService.getById("1");
        assertNotNull(user);

        instance.addMember(InstanceMember
            .newBuilder()
            .user(user)
            .role(OWNER)
            .build());

        instanceService.save(instance);
        Instance persistedInstance = instanceService.getById(1002L);

        assertEquals(2, persistedInstance.getMembers().size());
    }

    @Test
    @DisplayName("Add identical member to an instance does nothing")
    void addIdenticalMemberToInstance() {
        Instance instance = instanceService.getById(1002L);
        assertNotNull(instance);

        User user = userService.getById("1");
        assertNotNull(user);

        instance.addMember(InstanceMember
            .newBuilder()
            .user(user)
            .role(OWNER)
            .build());

        instanceService.save(instance);
        Instance persistedInstance = instanceService.getById(1002L);

        persistedInstance.addMember(InstanceMember
            .newBuilder()
            .user(user)
            .role(OWNER)
            .build());

        instanceService.save(persistedInstance);

        assertEquals(2, persistedInstance.getMembers().size());
    }

    @Test
    @DisplayName("Delete member from an instance")
    void deleteMemberFromInstance() {
        Instance instance = instanceService.getById(1000L);
        assertNotNull(instance);
        assertEquals(3, instance.getMembers().size());

        User user = userService.getById("1");
        assertNotNull(user);

        instance.removeMember(InstanceMember
            .newBuilder()
            .user(user)
            .role(OWNER)
            .build());

        instanceService.save(instance);
        Instance persistedInstance = instanceService.getById(1000L);

        assertEquals(2, persistedInstance.getMembers().size());
    }


    @Test
    @DisplayName("Get all inactive instances")
    public void inactiveInstances() {
        List<Instance> instances = instanceService.getAllInactive(InstanceExpirationService.HOURS_BEFORE_EXPIRATION_INACTIVITY);
        assertEquals(2, instances.size());
    }

    @Test
    @DisplayName("Get all active instances for an instrument scientist")
    public void instancesForInstrumentScientist() {
        User user1 = userService.getById("6");
        assertNotNull(user1);

        List<Instance> instances1 = this.instanceService.getAllForInstrumentScientist(user1, null, null, null);
        assertEquals(8, instances1.size());

        User user2 = userService.getById("5");
        assertNotNull(user1);

        List<Instance> instances2 = this.instanceService.getAllForInstrumentScientist(user2, null, null, null);
        assertEquals(1, instances2.size());
    }

    @Test
    @DisplayName("Get all active instances for an instrument scientist with filters")
    public void instancesForInstrumentScientistWithFilters() {
        User user1 = userService.getById("6");
        assertNotNull(user1);

        InstanceFilter filter = new InstanceFilter().id(1000L);
        List<Instance> instances = this.instanceService.getAllForInstrumentScientist(user1, filter, null, null);
        assertEquals(1, instances.size());

        filter = new InstanceFilter().name("Instance 1");
        instances = this.instanceService.getAllForInstrumentScientist(user1, filter, null, null);
        assertEquals(3, instances.size());

        filter = new InstanceFilter().owner("1");
        instances = this.instanceService.getAllForInstrumentScientist(user1, filter, null, null);
        assertEquals(2, instances.size());

        filter = new InstanceFilter().instrumentId(1L);
        instances = this.instanceService.getAllForInstrumentScientist(user1, filter, null, null);
        assertEquals(8, instances.size());

        filter = new InstanceFilter().instrumentId(2L);
        instances = this.instanceService.getAllForInstrumentScientist(user1, filter, null, null);
        assertEquals(0, instances.size());
    }

    @Test
    @DisplayName("Get all instances for instrument control support")
    public void instancesAllForScientificSupport() {
        List<Instance> instances = this.instanceService.getAllForInstrumentControlSupport(null, null, null);
        assertEquals(6 , instances.size());
    }

    @Test
    @DisplayName("Get all instances for instrument control support with filters")
    public void instancesAllForScientificSupportWithFilters() {
        InstanceFilter filter = new InstanceFilter().name("Instance 1");
        List<Instance> instances = this.instanceService.getAllForInstrumentControlSupport(filter, null, null);
        assertEquals(2 , instances.size());

        filter = new InstanceFilter().id(1008L);
        instances = this.instanceService.getAllForInstrumentControlSupport(filter, null, null);
        assertEquals(1 , instances.size());

        filter = new InstanceFilter().owner("1");
        instances = this.instanceService.getAllForInstrumentControlSupport(filter, null, null);
        assertEquals(1 , instances.size());

        filter = new InstanceFilter().instrumentId(1L);
        instances = this.instanceService.getAllForInstrumentControlSupport(filter, null, null);
        assertEquals(6 , instances.size());
    }

    Instance createInstanceWithExperiments(List<String> experimentIds) {
        Plan plan = planService.getById(1000L);

        Instance.Builder instanceBuilder = Instance.builder()
            .plan(plan)
            .name("Test instance")
            .comments("This is an instance")
            .screenWidth(1280)
            .screenHeight(1024)
            .keyboardLayout("en-gb-qwerty");

        Instance instance = instanceService.create(instanceBuilder);
        Long instanceId = instance.getId();
        assertNotNull(instanceId);

        for (String experimentId : experimentIds) {
            Experiment experiment = experimentService.getById(experimentId);
            instance.addExperiment(experiment);
        }
        instanceService.save(instance);

        return instance;
    }

    @Test
    @DisplayName("Get all instances for a given user")
    public void instancesForUser() {
        User user = userService.getById("1");
        assertNotNull(user);
        assertEquals(3, instanceService.getAllForUser(user).size());
    }

    @Test
    @DisplayName("Count all instances for a given user")
    public void countInstanceForUser() {
        User user = userService.getById("1");
        assertNotNull(user);
        assertEquals(2, instanceService.countAllForUserAndRole(user, OWNER));
    }
}
