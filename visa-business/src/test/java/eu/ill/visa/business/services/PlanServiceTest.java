package eu.ill.visa.business.services;

import jakarta.inject.Inject;
import eu.ill.visa.core.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.RollbackException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BusinessExtension.class)
public class PlanServiceTest {

    @Inject
    private PlanService planService;

    @Inject
    private FlavourService flavourService;

    @Inject
    private ImageService imageService;

    @Inject
    private InstrumentService instrumentService;

    @Inject
    private ExperimentService experimentService;

    @Test
    @DisplayName("Get an plan by a known id")
    void testGetById() {
        Long id = 1000L;
        Plan plan = planService.getById(id);
        assertEquals(id, plan.getId());
    }

    @Test
    @DisplayName("Get an plan by an unknown id")
    void testGetByUnknownId() {
        Plan plan = planService.getById(1000000L);
        assertNull(plan);
    }

    @Test
    @DisplayName("Get all plans")
    void testGetAll() {
        List<Plan> results = planService.getAll();
        assertEquals(4, results.size());
    }

    @Test
    @DisplayName("Delete an plan")
    void testDelete() {
        Plan plan = planService.getById(1002L);
        planService.delete(plan);
        List<Plan> results = planService.getAll();
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Should fail to delete an plan because there are instances associated to it")
    void testDeleteShouldFail() {
        Plan plan = planService.getById(1000L);
        assertThrows(RollbackException.class, () -> {
            planService.delete(plan);
        });
    }

    @Test
    @DisplayName("Create an plan")
    void testCreate() {
        Flavour flavour = flavourService.getById(1000L);
        Image image = imageService.getById(1000L);

        Plan.Builder builder = new Plan.Builder();
        builder
            .image(image)
            .flavour(flavour)
            .preset(false);
        Plan plan = builder.build();
        planService.save(plan);
        assertEquals(5, planService.getAll().size());
    }

    @Test
    @DisplayName("It should get all plans for a given list of instruments")
    void testGetAllPlansForInstruments() {
        final Instrument instrument1 = instrumentService.getById(1L);

        final List<Plan> plans = planService.getAllForInstruments(singletonList(instrument1));
        assertEquals(4, plans.size());

        final Instrument instrument2 = instrumentService.getById(5L);

        final List<Plan> plans2 = planService.getAllForInstruments(singletonList(instrument2));
        assertEquals(3, plans2.size());

        final List<Plan> plans3 = planService.getAllForInstruments(asList(instrument1, instrument2));
        assertEquals(4, plans3.size());
    }

    @Test
    @DisplayName("It should get all plans for a given list of experiments")
    void testGetAllPlansForExperiments() {
        final Experiment experiment1 = experimentService.getById("0001-0001-000001");
        final List<Plan> plans = planService.getAllForExperiments(singletonList(experiment1));
        assertEquals(4, plans.size());

        final Experiment experiment2 = experimentService.getById("0001-0005-000002");

        final List<Plan> plans2 = planService.getAllForExperiments(singletonList(experiment2));
        assertEquals(3, plans2.size());

        final List<Plan> plans3 = planService.getAllForExperiments(asList(experiment1, experiment2));
        assertEquals(4, plans3.size());
    }

}
