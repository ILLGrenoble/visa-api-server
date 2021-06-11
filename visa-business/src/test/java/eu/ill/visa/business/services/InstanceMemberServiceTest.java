package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.InstanceMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(BusinessExtension.class)
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
