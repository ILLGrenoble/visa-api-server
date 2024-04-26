package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.Flavour;
import eu.ill.visa.core.domain.FlavourLimit;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@TestTransaction
public class FlavourLimitServiceTest {

    @Inject
    private FlavourLimitService flavourLimitService;

    @Inject
    private FlavourService flavourService;

    @Test
    @DisplayName("Get all flavour limits")
    void testGetAll() {
        List<FlavourLimit> results = flavourLimitService.getAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Get a flavour limit by a known id")
    void testGetById() {
        Long id = 1000L;
        FlavourLimit flavour = flavourLimitService.getById(id);
        assertEquals(id, flavour.getId());
    }

    @Test
    @DisplayName("Get a flavour limit by an unknown id")
    void testGetByUnknownId() {
        FlavourLimit flavour = flavourLimitService.getById(1000000L);
        assertNull(flavour);
    }

    @Test
    @DisplayName("Delete a flavour limit")
    void testDelete() {
        FlavourLimit flavour = flavourLimitService.getById(1000L);
        flavourLimitService.delete(flavour);
        List<FlavourLimit> results = flavourLimitService.getAll();
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Create a flavour limit")
    void testCreate() {
        Flavour flavour = flavourService.getById(1001L);
        FlavourLimit.Builder builder = new FlavourLimit.Builder();
        FlavourLimit flavourLimit = builder
            .flavour(flavour)
            .objectId(1L)
            .objectType("INSTRUMENT")
            .build();

        flavourLimitService.save(flavourLimit);
        assertEquals(3, flavourLimitService.getAll().size());
    }

}
