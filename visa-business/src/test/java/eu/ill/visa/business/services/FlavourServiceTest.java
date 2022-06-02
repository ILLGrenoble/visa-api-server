package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.Flavour;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.persistence.RollbackException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BusinessExtension.class)
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

}
