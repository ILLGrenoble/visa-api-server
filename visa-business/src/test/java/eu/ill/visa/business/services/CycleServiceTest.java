package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.Cycle;
import eu.ill.visa.core.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@ExtendWith(BusinessExtension.class)
public class CycleServiceTest {

    @Inject
    private CycleService cycleService;

    @Inject
    private UserService userService;

    @Test
    @DisplayName("Get all cycles")
    void testGetAll() {
        List<Cycle> cycles = cycleService.getAll();
        assertEquals(6, cycles.size());
    }

    @Test
    @DisplayName("Get cycle by a known id")
    void testGetById() {
        Long id = 1L;
        Cycle cycle = cycleService.getById(id);
        assertEquals(id, cycle.getId());
        assertEquals("2016-1", cycle.getName());
    }

    @Test
    @DisplayName("Get cycle by an unknown id")
    void testGetByUnknownId() {
        Cycle cycle = cycleService.getById(1000000L);
        assertNull(cycle);
    }

    @Test
    @DisplayName("Get all for user")
    void testGetAllForUser() {
        User user = userService.getById("1");
        List<Cycle> cycles = cycleService.getAllForUser(user);
        assertEquals(2, cycles.size());
    }

}
