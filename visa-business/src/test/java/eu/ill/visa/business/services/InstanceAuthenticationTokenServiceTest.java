package eu.ill.visa.business.services;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import eu.ill.visa.core.domain.InstanceAuthenticationToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestTransaction
public class InstanceAuthenticationTokenServiceTest {

    @Inject
    private InstanceAuthenticationTokenService instanceAuthenticationTokenService;

    @Test
    @DisplayName("Get all instance authentication tokens")
    void testGetAll() {
        List<InstanceAuthenticationToken> results = instanceAuthenticationTokenService.getAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Get a instance by a known token")
    void testGetByToken() {
        String token = "24e7437a-eae5-48c4-923e-778c42a6acf8";
        InstanceAuthenticationToken instanceAuthenticationToken = this.instanceAuthenticationTokenService.getByToken(token);
        assertNotNull(instanceAuthenticationToken);
        assertEquals(1000L, instanceAuthenticationToken.getId());
        assertEquals(true, instanceAuthenticationToken.isExpired(10));
    }

}
