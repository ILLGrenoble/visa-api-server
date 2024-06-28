package eu.ill.visa.business.services;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.core.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
public class InstanceSessionServiceTest {

    @Inject
    private InstanceSessionService instanceSessionService;

    @Inject
    private InstanceService instanceService;

    @Inject
    private UserService userService;

    @Test
    @DisplayName("Get an instance session by a known id")
    void testGetById() {
        Long id = 1000L;
        InstanceSession instanceSession = instanceSessionService.getById(id);
        assertEquals(id, instanceSession.getId());
    }

    @Test
    @DisplayName("Get an instance session by a known instance")
    void testGetByInstance() {
        Instance instance = instanceService.getById(1000L);
        InstanceSession instanceSession = instanceSessionService.getByInstance(instance);
        assertNotNull(instanceSession);
    }

    @Test
    @DisplayName("Get an instance session by an unknown id")
    void testGetByUnknownId() {
        InstanceSession instanceSession = instanceSessionService.getById(1000000L);
        assertNull(instanceSession);
    }

    @Test
    @DisplayName("Get all instance sessions")
    void testGetAll() {
        List<InstanceSession> results = instanceSessionService.getAll();
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Delete an instance session")
    void testDelete() {
        InstanceSession instanceSession = instanceSessionService.getById(1000L);
        instanceSession.setCurrent(false);
        instanceSessionService.save(instanceSession);
        List<InstanceSession> results = instanceSessionService.getAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Create a new instance session")
    void create() {
        Instance instance = instanceService.getById(1000L);
        InstanceSession session = new InstanceSession(instance, "guacamole", "a-connection-id");
        instanceSessionService.save(session);
        InstanceSession persistedInstanceSession = instanceSessionService.getById(session.getId());
        assertNotNull(persistedInstanceSession);
        assertEquals("a-connection-id", persistedInstanceSession.getConnectionId());
    }

    @Test
    @DisplayName("Increment the client count for a given instance session")
    void incrementClientCountForSession() {
        User user = this.userService.getById("1");
        String sessionId = UUID.randomUUID().toString();

        InstanceSession instanceSession = instanceSessionService.getById(1000L);
        List<InstanceSessionMember> members1 = instanceSessionService.getAllSessionMembersByInstanceSession(instanceSession);
        instanceSessionService.addInstanceSessionMember(instanceSession, sessionId, user, "OWNER");
        List<InstanceSessionMember> members2 = instanceSessionService.getAllSessionMembersByInstanceSession(instanceSession);
        assertEquals(members1.size() + 1, members2.size());
    }

    @Test
    @DisplayName("Decrement the client count for a given instance session")
    void decrementClientCountForSession() {
        InstanceSession instanceSession = instanceSessionService.getById(1000L);
        List<InstanceSessionMember> members1 = instanceSessionService.getAllSessionMembersByInstanceSession(instanceSession);
        instanceSessionService.removeInstanceSessionMember(instanceSession, "24e7437a-eae5-48c4-823e-778c42a6acf8");
        List<InstanceSessionMember> members2 = instanceSessionService.getAllSessionMembersByInstanceSession(instanceSession);
        assertEquals(members1.size() - 1, members2.size());
    }

    @Test
    @DisplayName("Test deleted when client count zero")
    void testDeletedWhenClientCountZero() {
        User user = this.userService.getById("1");
        String sessionId = UUID.randomUUID().toString();

        Instance instance = instanceService.getById(1000L);
        InstanceSession session = instanceSessionService.create(instance, "guacamole", "a-connection-id");

        InstanceSession persistedInstanceSession = instanceSessionService.getById(session.getId());
        assertNotNull(persistedInstanceSession);
        List<InstanceSessionMember> members1 = instanceSessionService.getAllSessionMembersByInstanceSession(persistedInstanceSession);
        assertEquals(0, members1.size());

        instanceSessionService.addInstanceSessionMember(persistedInstanceSession, sessionId, user, "OWNER");
        List<InstanceSessionMember> members2 = instanceSessionService.getAllSessionMembersByInstanceSession(persistedInstanceSession);
        assertEquals(1, members2.size());
        instanceSessionService.removeInstanceSessionMember(persistedInstanceSession, sessionId);
        List<InstanceSessionMember> members3 = instanceSessionService.getAllSessionMembersByInstanceSession(persistedInstanceSession);
        assertEquals(0, members3.size());

        InstanceSession deletedInstanceSession = instanceSessionService.getById(session.getId());
        assertNotNull(deletedInstanceSession);
        assertEquals(false, deletedInstanceSession.getCurrent());
    }


}
