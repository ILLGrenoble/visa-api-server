package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.Image;
import eu.ill.visa.core.domain.ImageProtocol;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
public class ImageServiceTest {

    @Inject
    private ImageService imageService;

    @Inject
    private ImageProtocolService protocolService;

    @Test
    @DisplayName("Get an image by a known id")
    void testGetById() {
        Long id = 1000L;
        Image image = imageService.getById(id);
        assertEquals(id, image.getId());
        assertEquals("Image 1", image.getName());
    }

    @Test
    @DisplayName("Get an image by an unknown id")
    void testGetByUnknownId() {
        Image image = imageService.getById(1000000L);
        assertNull(image);
    }

    @Test
    @DisplayName("Get all images")
    void testGetAll() {
        List<Image> results = imageService.getAll();
        assertEquals(4, results.size());
    }

    @Test
    @DisplayName("Delete an image")
    void testDelete() {
        Image image = imageService.getById(1003L);
        imageService.delete(image);
        List<Image> results = imageService.getAll();
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Should fail to delete an image because there are instances associated to it")
    void testDeleteShouldFail() {
        Image image = imageService.getById(1000L);
        assertThrows(RollbackException.class, () -> {
            imageService.delete(image);
        });
    }

    @Test
    @DisplayName("Create an image")
    void testCreate() {
        Image.Builder builder = new Image.Builder();
        builder
            .name("My test image")
            .description("My test image description")
            .computeId(UUID.randomUUID().toString())
            .icon("New icon")
            .bootCommand("Hello world!")
            .visible(true);
        Image image = builder.build();
        imageService.save(image);
        assertEquals(5, imageService.getAll().size());
    }

    @Test
    @DisplayName("Should add two new protocols to an image")
    void testAddProtocolToImage() {
        final ImageProtocol protocol1 = protocolService.getById(1000L);
        final ImageProtocol protocol2 = protocolService.getById(1001L);
        final Image image = imageService.getById(1000L);
        image.addProtocol(protocol1);
        image.addProtocol(protocol2);
        imageService.save(image);
        assertEquals(2, imageService.getById(1000L).getProtocols().size());
    }

}
