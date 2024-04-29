package eu.ill.visa.business.services;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import eu.ill.visa.core.entity.InstanceMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@TestTransaction
public class InstanceMemberServiceTest {

    @Inject
    private InstanceMemberService memberService;

    @Inject
    private UserService userService;

    @Inject
    private InstanceService instanceService;

    @Test
    @DisplayName("Get an instance member by a known id")
    void testGetById() {
        Long id = 1000L;
        InstanceMember member = memberService.getById(id);
        assertEquals(id, member.getId());
    }

    @Test
    @DisplayName("Get an instance member by an unknown id")
    void testGetByUnknownId() {
        InstanceMember member = memberService.getById(1000000L);
        assertNull(member);
    }

    @Test
    @DisplayName("Get all instance members")
    void testGetAll() {
        List<InstanceMember> results = memberService.getAll();
        assertEquals(15, results.size());
    }
}
