package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BusinessExtension.class)
public class UserServiceTest {

    @Inject
    private UserService userService;

    @Inject
    private InstanceService instanceService;

    @Test
    @DisplayName("Get all users that match a last name wildcard")
    void testGetAllLikeLastName() {
        List<User> users = userService.getAllLikeLastName("cla", true);
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Count all users for a given role")
    void testGetAllForRole() {
        final Long count = userService.countAllUsersForRole("ADMIN");
        assertEquals(count, 2);
    }

    @Test
    @DisplayName("Get the experimental team for a given instance")
    void testGetExperimentalTeamForInstance() {
        Instance instance = instanceService.getById(1000L);
        List<User> users = userService.getExperimentalTeamForInstance(instance);
        assertNotNull(users);
    }

    @Test
    @DisplayName("Get all users that match a last name wildcard with pagination")
    void testGetAllLikeLastNameWithPagination() {
        final Pagination pagination = new Pagination(1, 1);
        List<User> users = userService.getAllLikeLastName("cla", true, pagination);
        assertEquals(1, users.size());
    }

    @Test
    @DisplayName("Get a user by a known id")
    void testGetById() {
        String id = "1";
        User user = userService.getById(id);
        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals("bloggs@example.com", user.getEmail());
        assertEquals("Joe Bloggs", user.getFullName());
    }


    @Test
    @DisplayName("Get a user by an unknown id")
    void testGetByUnknownId() {
        User user = userService.getById("1000000");
        assertNull(user);
    }


    @Test
    @DisplayName("Create a user")
    void testCreate() {

        User.Builder builder = new User.Builder();
        builder
            .id("999")
            .firstName("Peter")
            .lastName("Parker")
            .activatedAt(new Date())
            .email("parker@example.com")
            .instanceQuota(2);
        User user = builder.build();
        userService.save(user);
        User createdUser = userService.getById("999");
        assertNotNull(createdUser);
    }


}
