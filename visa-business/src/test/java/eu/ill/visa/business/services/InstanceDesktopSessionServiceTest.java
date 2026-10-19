package eu.ill.visa.business.services;

import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import eu.ill.visa.core.entity.InstanceDesktopSession;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
public class InstanceDesktopSessionServiceTest {

    @Inject
    InstanceDesktopSessionService instanceDesktopSessionService;

    @Test
    @DisplayName("Get an instance desktop session by a known instance")
    void testGetByInstance() {
        List<InstanceDesktopSession> instanceSessions = instanceDesktopSessionService.getAllByInstanceId(1000L);
        assertEquals(2, instanceSessions.size());
    }

    @Test
    @DisplayName("Get an instance desktop session by an unknown instance id")
    void testGetByUnknownId() {
        List<InstanceDesktopSession> instanceSessions = instanceDesktopSessionService.getAllByInstanceId(10000L);
        assertEquals(0, instanceSessions.size());
    }

    @Test
    @DisplayName("Get all instance desktop sessions")
    void testGetAll() {
        List<InstanceDesktopSession> results = instanceDesktopSessionService.getAll();
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Delete an instance desktop session")
    void testDelete() {
        InstanceDesktopSession instanceDesktopSession = instanceDesktopSessionService.getById(1000L);
        instanceDesktopSessionService.close(instanceDesktopSession);
        List<InstanceDesktopSession> results = instanceDesktopSessionService.getAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Create a new instance desktop session")
    void create() {
        assertEquals(3, instanceDesktopSessionService.getAll().size());

        InstanceDesktopSession session = instanceDesktopSessionService.create("a-connection-id", "a-client-id", 1004L, "1", InstanceMemberRole.OWNER, InstanceDesktopSession.WEBX_PROTOCOL);
        InstanceDesktopSession persistedInstanceSession = instanceDesktopSessionService.getById(session.getId());
        assertNotNull(persistedInstanceSession);
        assertEquals("a-connection-id", persistedInstanceSession.getConnectionId());

        assertEquals(4, instanceDesktopSessionService.getAll().size());
    }

//    @Test
//    @DisplayName("Increment the client count for a given instance session")
//    void incrementClientCountForSession() {
//        User user = this.userService.getById("1");
//        String clientId = UUID.randomUUID().toString();
//
//        InstanceSession instanceSession = instanceSessionService.getById(1000L);
//        List<InstanceSessionMemberPartial> members1 = instanceSessionMemberService.getAllPartialsByInstanceSessionId(instanceSession.getId());
//        instanceSessionMemberService.create(instanceSession, clientId, user, InstanceMemberRole.OWNER);
//        List<InstanceSessionMemberPartial> members2 = instanceSessionMemberService.getAllPartialsByInstanceSessionId(instanceSession.getId());
//        assertEquals(members1.size() + 1, members2.size());
//    }
//
//    @Test
//    @DisplayName("Decrement the client count for a given instance session")
//    void decrementClientCountForSession() {
//        InstanceSession instanceSession = instanceSessionService.getById(1000L);
//        List<InstanceSessionMemberPartial> members1 = instanceSessionMemberService.getAllPartialsByInstanceSessionId(instanceSession.getId());
//        instanceSessionService.deleteSessionMember(instanceSession, "24e7437a-eae5-48c4-823e-778c42a6acf8");
//        List<InstanceSessionMemberPartial> members2 = instanceSessionMemberService.getAllPartialsByInstanceSessionId(instanceSession.getId());
//        assertEquals(members1.size() - 1, members2.size());
//    }
//
//    @Test
//    @DisplayName("Test deleted when client count zero")
//    void testDeletedWhenClientCountZero() {
//        User user = this.userService.getById("1");
//        String clientId = UUID.randomUUID().toString();
//
//        Instance instance = instanceService.getById(1000L);
//        InstanceSession session = instanceSessionService.create(instance.getId(), InstanceSession.GUACAMOLE_PROTOCOL, "a-connection-id");
//
//        InstanceSession persistedInstanceSession = instanceSessionService.getById(session.getId());
//        assertNotNull(persistedInstanceSession);
//        List<InstanceSessionMemberPartial> members1 = instanceSessionMemberService.getAllPartialsByInstanceSessionId(persistedInstanceSession.getId());
//        assertEquals(0, members1.size());
//
//        instanceSessionMemberService.create(persistedInstanceSession, clientId, user, InstanceMemberRole.OWNER);
//        List<InstanceSessionMemberPartial> members2 = instanceSessionMemberService.getAllPartialsByInstanceSessionId(persistedInstanceSession.getId());
//        assertEquals(1, members2.size());
//        instanceSessionService.deleteSessionMember(persistedInstanceSession, clientId);
//        List<InstanceSessionMemberPartial> members3 = instanceSessionMemberService.getAllPartialsByInstanceSessionId(persistedInstanceSession.getId());
//        assertEquals(0, members3.size());
//
//        InstanceSession deletedInstanceSession = instanceSessionService.getById(session.getId());
//        assertNotNull(deletedInstanceSession);
//        assertEquals(false, deletedInstanceSession.getCurrent());
//    }


}
