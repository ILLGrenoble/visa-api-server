package eu.ill.visa.business.services;

import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceExpiration;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@TestTransaction
public class InstanceExpirationServiceTest {

    @Inject
    private InstanceExpirationService instanceExpirationService;

    @Inject
    private InstanceConfiguration instanceConfiguration;

    @Inject
    private InstanceService instanceService;

    @Test
    @DisplayName("Get an instanceExpiration by a known id")
    void testGetById() {
        Long id = 1000L;
        InstanceExpiration instanceExpiration = instanceExpirationService.getById(id);
        assertEquals(id, instanceExpiration.getId());
        assertEquals(1000L, instanceExpiration.getInstance().getId());
    }

    @Test
    @DisplayName("Get an instanceExpiration by an unknown id")
    void testGetByUnknownId() {
        InstanceExpiration instanceExpiration = instanceExpirationService.getById(1000000L);
        assertNull(instanceExpiration);
    }

    @Test
    @DisplayName("Get all instanceExpirations")
    void testGetAll() {
        List<InstanceExpiration> results = instanceExpirationService.getAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Delete an instanceExpiration")
    void testDelete() {
        InstanceExpiration instanceExpiration = instanceExpirationService.getById(1001L);
        instanceExpirationService.delete(instanceExpiration);
        List<InstanceExpiration> results = instanceExpirationService.getAll();
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Create an instanceExpiration")
    void testCreate() {
        Instance instance = instanceService.getById(1002L);
        instanceExpirationService.create(instance, new Date());
        assertEquals(3, instanceExpirationService.getAll().size());
    }

    @Test
    @DisplayName("Does not create an instanceExpiration for an instance already to be expired")
    void testDoesNotCreate() {
        Instance instance = instanceService.getById(1000L);
        InstanceExpiration instanceExpiration = instanceExpirationService.create(instance, new Date());
        assertEquals(1000L, instanceExpiration.getId());
        assertEquals(2, instanceExpirationService.getAll().size());
    }

    @Test
    @DisplayName("Get all expired instances")
    void testGetAllExpired() {
        List<InstanceExpiration> results = instanceExpirationService.getAllExpired();
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Check an instance expires in 1 second")
    void testCreateAndExpire() throws InterruptedException {
        int sleepMs = 1000;
        long time = (new Date().getTime()) + sleepMs;
        Instance instance = instanceService.getById(1002L);
        InstanceExpiration instanceExpiration = instanceExpirationService.create(instance, new Date(time));
        assertEquals(3, instanceExpirationService.getAll().size());
        assertEquals(1, instanceExpirationService.getAllExpired().size());

        Thread.sleep(sleepMs);
        assertEquals(2, instanceExpirationService.getAllExpired().size());
    }

    @Test
    @DisplayName("Delete expired instances")
    void testDeleteInstanceExpiration() {
        List<InstanceExpiration> expirations1 = instanceExpirationService.getAllExpired();
        List<Instance> instances1 = instanceService.getAll();
        for (InstanceExpiration instanceExpiration : expirations1) {
            instanceExpirationService.delete(instanceExpiration);
        }
        List<InstanceExpiration> expirations2 = instanceExpirationService.getAllExpired();
        List<Instance> instances2 = instanceService.getAll();

        assertEquals(0, expirations2.size());
        assertEquals(instances1.size(), instances2.size());
    }

    @Test
    @DisplayName("Removes an instance expiration due to activity")
    void testRemoveInstanceExpirationDueToActivity() {

        long time = (new Date().getTime()) + 10000;
        Instance instance = instanceService.getById(1002L);
        instanceExpirationService.create(instance, new Date(time));
        assertEquals(3, instanceExpirationService.getAll().size());

        instance.setLastSeenAt(new Date());
        instanceService.save(instance);

        instanceExpirationService.removeExpirationForAllActiveInstances();
        assertEquals(2, instanceExpirationService.getAll().size());
    }

    @Test
    @DisplayName("Creates an instance expiration due to inactivity")
    void testCreateInstanceExpirationDueToInactivity() {

        var initialExpirations = instanceExpirationService.getAll();

        // Test created for existing fixtures (no new ones)
        instanceExpirationService.createExpirationForAllInactiveInstances();
        assertEquals(initialExpirations.size(), instanceExpirationService.getAll().size());

        Date lastActiveTime = DateUtils.addHours(new Date(), -this.instanceConfiguration.userMaxInactivityDurationHours() + 24);
        Instance instance = instanceService.getById(1002L);
        instance.setLastSeenAt(lastActiveTime);
        instanceService.save(instance);

        // Test created for new one
        instanceExpirationService.createExpirationForAllInactiveInstances();
        assertEquals(initialExpirations.size() + 1, instanceExpirationService.getAll().size());

        // Test not recreated
        instanceExpirationService.createExpirationForAllInactiveInstances();
        assertEquals(initialExpirations.size() + 1, instanceExpirationService.getAll().size());
    }

    @Test
    @DisplayName("Creates an instance expiration due to termination date")
    void testCreateInstanceExpirationDueToTerminationDate() throws InterruptedException {

        var initialExpirations = instanceExpirationService.getAll();

        // Test created for existing fixtures (no new ones)
        instanceExpirationService.createExpirationForAllTerminatingInstances();
        assertEquals(initialExpirations.size(), instanceExpirationService.getAll().size());

        Date terminationDate = DateUtils.addHours(new Date(), InstanceExpirationService.HOURS_BEFORE_EXPIRATION_LIFETIME);
        Instance instance = instanceService.getById(1002L);
        instance.setTerminationDate(terminationDate);
        instanceService.save(instance);

        // Test created for new one
        instanceExpirationService.createExpirationForAllTerminatingInstances();
        assertEquals(initialExpirations.size() + 1, instanceExpirationService.getAll().size());

        // Test not recreated
        instanceExpirationService.createExpirationForAllTerminatingInstances();
        assertEquals(initialExpirations.size() + 1, instanceExpirationService.getAll().size());
    }


    @Test
    @DisplayName("Does not create an instance expiration due to inactivity for an instance with no termination date")
    void testCreateInstanceExpirationDueToInactivityForNullTerminationDate() {

        var initialExpirations = instanceExpirationService.getAll();

        // Test created for existing fixtures (no new ones)
        instanceExpirationService.createExpirationForAllInactiveInstances();
        assertEquals(initialExpirations.size(), instanceExpirationService.getAll().size());

        Date lastActiveTime = DateUtils.addHours(new Date(), -this.instanceConfiguration.userMaxInactivityDurationHours() + InstanceExpirationService.HOURS_BEFORE_EXPIRATION_INACTIVITY);
        Instance instance = instanceService.getById(1002L);
        instance.setLastSeenAt(lastActiveTime);
        instance.setTerminationDate(null);
        instanceService.save(instance);

        // Test doesn't create a new one
        instanceExpirationService.createExpirationForAllInactiveInstances();
        assertEquals(initialExpirations.size(), instanceExpirationService.getAll().size());
    }

}
