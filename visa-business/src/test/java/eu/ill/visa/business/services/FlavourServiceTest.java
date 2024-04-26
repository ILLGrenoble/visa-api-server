package eu.ill.visa.business.services;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import eu.ill.visa.core.domain.Flavour;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.RollbackException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
public class FlavourServiceTest {

    @Inject
    private FlavourService flavourService;

    @Test
    @DisplayName("Get all flavours")
    void testGetAll() {
        List<Flavour> results = flavourService.getAll();
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Get a flavour by a known id")
    void testGetById() {
        Long id = 1000L;
        Flavour flavour = flavourService.getById(id);
        assertEquals(id, flavour.getId());
    }

    @Test
    @DisplayName("Get a flavour by an unknown id")
    void testGetByUnknownId() {
        Flavour flavour = flavourService.getById(1000000L);
        assertNull(flavour);
    }

    @Test
    @DisplayName("Delete a flavour")
    void testDelete() {
        Flavour flavour = flavourService.getById(1002L);
        flavourService.delete(flavour);
        List<Flavour> results = flavourService.getAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Failed to delete a flavour because plans are associated to it")
    void testFailedToDeleteBecausePlansAreAssociatedToIt() {
        assertThrows(RollbackException.class, () -> {
            Flavour flavour = flavourService.getById(1000L);
            flavourService.delete(flavour);
        });
    }
    @Test
    @DisplayName("Create a flavour")
    void testCreate() {
        Flavour.Builder builder = new Flavour.Builder();
        builder
            .name("My test flavour")
            .computeId(UUID.randomUUID().toString())
            .cpu(0.5f)
            .memory(2048);

        Flavour flavour = builder.build();
        flavourService.create(flavour);
        assertEquals(4, flavourService.getAll().size());
    }


}
