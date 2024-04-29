package eu.ill.visa.business.services;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import eu.ill.visa.core.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@TestTransaction
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
