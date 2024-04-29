package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Proposal;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


@QuarkusTest
@TestTransaction
public class ProposalServiceTest {

    @Inject
    private ProposalService proposalService;

    @Test
    @DisplayName("Get all proposals")
    void testGetAll() {
        List<Proposal> proposals = proposalService.getAll();

        assertEquals(5, proposals.size());
    }

    @Test
    @DisplayName("Get proposal by a known id")
    void testGetById() {
        Long id = 1L;
        Proposal proposal = proposalService.getById(id);

        assertEquals(id, proposal.getId());
        assertEquals("PRO-1", proposal.getIdentifier());
        assertEquals("Proposal 1 title", proposal.getTitle());
    }


    @Test
    @DisplayName("Get proposal by an unknown id")
    void testGetByUnknownId() {
        Proposal proposal = proposalService.getById(1000000L);
        assertNull(proposal);
    }
}
