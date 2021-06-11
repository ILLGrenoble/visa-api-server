package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.persistence.RollbackException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        List<Role> results = roleService.getAll();
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Failed to delete a role because users are associated to it")
    void testFailToDeleteBecauseUsersAreAssociatedToIt() {
        assertThrows(RollbackException.class, () -> {
            Role role = roleService.getById(1000L);
            roleService.delete(role);
            List<Role> results = roleService.getAll();
            assertEquals(2, results.size());
        });
    }


}
