package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceSession;
import eu.ill.visa.core.domain.InstanceSessionMember;
import eu.ill.visa.core.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BusinessExtension.class)
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
        InstanceSession session = new InstanceSession(instance, "a-connection-id");
        instanceSessionService.save(session);
        InstanceSession persistedInstanceSession = instanceSessionService.getById(session.getId());
        assertNotNull(persistedInstanceSession);
        assertEquals("a-connection-id", persistedInstanceSession.getConnectionId());
    }

    @Test
    @DisplayName("Increment the client count for a given instance session")
    void incrementClientCountForSession() {
        User user = this.userService.getById("1");
        UUID sessionId = UUID.randomUUID();

        InstanceSession instanceSession = instanceSessionService.getById(1000L);
        List<InstanceSessionMember> members1 = instanceSessionService.getAllSessionMembers(instanceSession);
        instanceSessionService.addInstanceSessionMember(instanceSession, sessionId, user, "OWNER");
        List<InstanceSessionMember> members2 = instanceSessionService.getAllSessionMembers(instanceSession);
        assertEquals(members1.size() + 1, members2.size());
    }

    @Test
    @DisplayName("Decrement the client count for a given instance session")
    void decrementClientCountForSession() {
        InstanceSession instanceSession = instanceSessionService.getById(1000L);
        List<InstanceSessionMember> members1 = instanceSessionService.getAllSessionMembers(instanceSession);
        instanceSessionService.removeInstanceSessionMember(instanceSession, UUID.fromString("24e7437a-eae5-48c4-823e-778c42a6acf8"));
        List<InstanceSessionMember> members2 = instanceSessionService.getAllSessionMembers(instanceSession);
        assertEquals(members1.size() - 1, members2.size());
    }

    @Test
    @DisplayName("Test deleted when client count zero")
    void testDeletedWhenClientCountZero() {
        User user = this.userService.getById("1");
        UUID sessionId = UUID.randomUUID();

        Instance instance = instanceService.getById(1000L);
        InstanceSession session = instanceSessionService.create(instance, "a-connection-id");

        InstanceSession persistedInstanceSession = instanceSessionService.getById(session.getId());
        assertNotNull(persistedInstanceSession);
        List<InstanceSessionMember> members1 = instanceSessionService.getAllSessionMembers(persistedInstanceSession);
        assertEquals(0, members1.size());

        instanceSessionService.addInstanceSessionMember(persistedInstanceSession, sessionId, user, "OWNER");
        List<InstanceSessionMember> members2 = instanceSessionService.getAllSessionMembers(persistedInstanceSession);
        assertEquals(1, members2.size());
        instanceSessionService.removeInstanceSessionMember(persistedInstanceSession, sessionId);
        List<InstanceSessionMember> members3 = instanceSessionService.getAllSessionMembers(persistedInstanceSession);
        assertEquals(0, members3.size());

        InstanceSession deletedInstanceSession = instanceSessionService.getById(session.getId());
        assertNotNull(deletedInstanceSession);
        assertEquals(false, deletedInstanceSession.getCurrent());
    }


}
