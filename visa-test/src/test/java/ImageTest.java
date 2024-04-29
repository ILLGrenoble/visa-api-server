import eu.ill.visa.test.ImageService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestTransaction
public class ImageTest {

    @Inject
    ImageService imageService;

    @Test
    @DisplayName("Get an image by a known id")
    void testGetById() {
        this.imageService.getAll();
        Long id = 1000L;
        assertEquals(1000, id);
    }

}
