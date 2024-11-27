package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.filters.ExperimentFilter;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.Experiment;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
public class ExperimentServiceTest {

    @Inject
    private ExperimentService experimentService;

    @Inject
    private UserService userService;

    @Inject
    private InstrumentService instrumentService;

    @Inject
    private InstanceService instanceService;

    @Test
    @DisplayName("Get all experiments")
    void testGetAll() {
        List<Experiment> experiments = experimentService.getAll(new ExperimentFilter());
        assertEquals(9, experiments.size());
    }

    @Test
    @DisplayName("Get an experiment by a known id")
    void testGetById() {
        String id = "0001-0001-000001";
        Experiment experiment = experimentService.getById(id);
        assertNotNull(experiment);
        assertEquals(id, experiment.getId());
        assertEquals("I1", experiment.getInstrument().getName());
        assertEquals("PRO-1", experiment.getProposal().getIdentifier());
    }

    @Test
    @DisplayName("Get an experiment by an unknown id")
    void testGetByUnknownId() {
        Experiment experiment = experimentService.getById("1234-1234-123456");
        assertNull(experiment);
    }

    @Test
    @DisplayName("Get all experiments for a given user")
    void testGetAllForUser() {
        ExperimentFilter filter = new ExperimentFilter();
        filter.setUserId("1");
        List<Experiment> experiments = experimentService.getAll(filter);
        assertEquals(3, experiments.size());
    }

    @Test
    @DisplayName("Count all experiments for a given user")
    void testCountAllForUser() {
        ExperimentFilter filter = new ExperimentFilter();
        filter.setUserId("1");
        Long count = experimentService.countAll(filter);
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Get all experiments for a given user and dates")
    void testGetAllAndDates() {
        List<Experiment> experiments = new ArrayList<>();
        try {
            ExperimentFilter filter = new ExperimentFilter();
            filter.setUserId("1");
            filter.setStartDate(ExperimentFilter.DateParameter.valueOf("2016-01-01"));
            filter.setEndDate(ExperimentFilter.DateParameter.valueOf("2016-01-05"));
            experiments = experimentService.getAll(filter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(2, experiments.size());
    }

    @Test
    @DisplayName("Count all experiments for a given user and dates")
    void testCountAllForUserAndDates() {
        Long experiments = 0L;
        try {
            ExperimentFilter filter = new ExperimentFilter();
            filter.setUserId("1");
            filter.setStartDate(ExperimentFilter.DateParameter.valueOf("2016-01-01"));
            filter.setEndDate(ExperimentFilter.DateParameter.valueOf("2016-01-05"));
            experiments = experimentService.countAll(filter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(2, experiments);
    }


    @Test
    @DisplayName("Get all experiments for a given user and experiment dates with pagination")
    void testGetAllAndDatesWithPagination() {
        List<Experiment> experiments = new ArrayList<>();
        try {
            ExperimentFilter filter = new ExperimentFilter();
            filter.setUserId("1");
            filter.setStartDate(ExperimentFilter.DateParameter.valueOf("2016-01-01"));
            filter.setEndDate(ExperimentFilter.DateParameter.valueOf("2016-01-05"));
            Pagination pagination = new Pagination(1, 1);
            experiments = experimentService.getAll(filter, pagination);

        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(1, experiments.size());
    }

    @Test
    @DisplayName("Get all experiments for a given user and instrument")
    void testGetAllAndInstrument() {
        ExperimentFilter filter = new ExperimentFilter();
        filter.setUserId("1");
        filter.setInstrumentId(1L);
        List<Experiment> experiments = experimentService.getAll(filter);
        assertEquals(2, experiments.size());
    }

    @Test
    @DisplayName("Count all experiments for a given user and instrument")
    void testCountAllForUserAndInstrument() {
        ExperimentFilter filter = new ExperimentFilter();
        filter.setUserId("1");
        filter.setInstrumentId(1L);
        Long experiments = experimentService.countAll(filter);
        assertEquals(2, experiments);
    }

    @Test
    @DisplayName("Get an experiment by a given id and user")
    void testGetByIdAndUser() {
        User user = userService.getById("1");
        Experiment experiment = experimentService.getByIdAndUser("0001-0001-000001", user);
        assertNotNull(experiment);
    }

    @Test
    @DisplayName("Get all experiments for a given instrument")
    void testGetAllFilteredByInstrument() {
        ExperimentFilter filter = new ExperimentFilter();
        filter.setInstrumentId(1L);
        List<Experiment> experiments = experimentService.getAll(filter, new Pagination(50, 0));
        assertEquals(2, experiments.size());
    }

    @Test
    @DisplayName("Add experiment to instance")
    void testAddExperimentToInstance() {
        Instance instance = instanceService.getById(1001L);

        Experiment experiment1 = experimentService.getById("0001-0001-000001");
        Experiment experiment2 = experimentService.getById("0001-0002-000002");

        instance.addExperiment(experiment1);
        instance.addExperiment(experiment2);

        instanceService.save(instance);

        Set<Experiment> experiments = experimentService.getAllForInstance(instance);

        assertEquals(2, experiments.size());
    }

}
