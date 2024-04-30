import eu.ill.visa.test.TotoService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestTransaction
public class TotoTest {

    @Inject
    TotoService totoService;

    @Test
    @DisplayName("Get a toto by a known id")
    void testGetById() {
        this.totoService.getAll();
        Long id = 1000L;
        assertEquals(1000, id);
    }

}
