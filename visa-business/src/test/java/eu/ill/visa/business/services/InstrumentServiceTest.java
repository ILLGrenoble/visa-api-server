package eu.ill.visa.business.services;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import eu.ill.visa.core.domain.Instrument;
import eu.ill.visa.core.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@QuarkusTest
@TestTransaction
public class InstrumentServiceTest {

    @Inject
    private InstrumentService instrumentService;

    @Inject
    private UserService userService;

    @Test
    @DisplayName("Get all instruments")
    void testGetAll() {
        List<Instrument> instruments = instrumentService.getAll();
        assertEquals(4, instruments.size());
    }

    @Test
    @DisplayName("Get instrument by a known id")
    void testGetById() {
        Long id = 1L;
        Instrument instrument = instrumentService.getById(id);
        assertEquals(id, instrument.getId());
        assertEquals("I1", instrument.getName());
    }

    @Test
    @DisplayName("Get instrument by an unknown id")
    void testGetByUnknownId() {
        Instrument instrument = instrumentService.getById(1000000L);
        assertNull(instrument);
    }

    @Test
    @DisplayName("Get all for user")
    void testGetAllForUser() {
        User user = userService.getById("1");
        List<Instrument> instruments = instrumentService.getAllForUser(user);
        assertEquals(2, instruments.size());
    }

}
