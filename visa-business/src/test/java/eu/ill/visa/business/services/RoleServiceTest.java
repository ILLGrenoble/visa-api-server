package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(BusinessExtension.class)
public class RoleServiceTest {

    @Inject
    private RoleService roleService;

    @Test
    @DisplayName("Get a role by a known id")
    void testGetById() {
        Long id = 1000L;
        Role role = roleService.getById(id);
        assertEquals(id, role.getId());
        assertEquals("ADMIN", role.getName());
    }

    @Test
    @DisplayName("Get a role by an unknown id")
    void testGetByUnknownId() {
        Role protocol = roleService.getById(1000000L);
        assertNull(protocol);
    }

    @Test
    @DisplayName("Get all roles")
    void testGetAll() {
        List<Role> results = roleService.getAllRoles();
        assertEquals(3, results.size());
    }

}
