package eu.ill.visa.business.services;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import eu.ill.visa.core.domain.ImageProtocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@TestTransaction
public class ImageProtocolServiceTest {

    @Inject
    private ImageProtocolService imageProtocolService;

    @Test
    @DisplayName("Get an image protocol by a known id")
    void testGetById() {
        Long id = 1000L;
        ImageProtocol protocol = imageProtocolService.getById(id);
        assertEquals(id, protocol.getId());
        assertEquals("GUACD", protocol.getName());
    }

    @Test
    @DisplayName("Get an image protocol by a known name")
    void testGetByName() {
        ImageProtocol protocol = imageProtocolService.getByName("GUACD");
        assertEquals("GUACD", protocol.getName());
    }

    @Test
    @DisplayName("Get an image protocol by an unknown id")
    void testGetByUnknownId() {
        ImageProtocol protocol = imageProtocolService.getById(1000000L);
        assertNull(protocol);
    }

    @Test
    @DisplayName("Get all image protocol")
    void testGetAll() {
        List<ImageProtocol> results = imageProtocolService.getAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Delete an image protocol")
    void testDelete() {
        ImageProtocol protocol = imageProtocolService.getById(1001L);
        imageProtocolService.delete(protocol);
        List<ImageProtocol> results = imageProtocolService.getAll();
        assertEquals(1, results.size());
    }


}
