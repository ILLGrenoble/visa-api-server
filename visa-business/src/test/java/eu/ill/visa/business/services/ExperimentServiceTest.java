package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BusinessExtension.class)
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
        List<Experiment> experiments = experimentService.getAll();
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
        User user = userService.getById("1");
        List<Experiment> experiments = experimentService.getAllForUser(user);
        assertEquals(3, experiments.size());
    }

    @Test
    @DisplayName("Count all experiments for a given user")
    void testCountAllForUser() {
        User user = userService.getById("1");
        Long count = experimentService.getAllCountForUser(user);
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Get all experiments for a given user and dates")
    void testGetAllForUserAndDates() {
        List<Experiment> experiments = new ArrayList<>();
        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-01");
            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-05");
            User user = userService.getById("1");
            ExperimentFilter filter = new ExperimentFilter(startDate, endDate);
            experiments = experimentService.getAllForUser(user, filter);

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
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-01");
            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-05");
            User user = userService.getById("1");
            ExperimentFilter filter = new ExperimentFilter(startDate, endDate);
            experiments = experimentService.getAllCountForUser(user, filter);

        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(2, experiments);
    }


    @Test
    @DisplayName("Get all experiments for a given user and experiment dates with pagination")
    void testGetAllForUserAndDatesWithPagination() {
        List<Experiment> experiments = new ArrayList<>();
        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-01");
            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-05");
            User user = userService.getById("1");
            ExperimentFilter filter = new ExperimentFilter(startDate, endDate);
            Pagination pagination = new Pagination(1, 1);
            experiments = experimentService.getAllForUser(user, filter, pagination);

        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(1, experiments.size());
    }

    @Test
    @DisplayName("Get all experiments for a given user and instrument")
    void testGetAllForUserAndInstrument() {
        Instrument instrument = instrumentService.getById(1L);
        User user = userService.getById("1");
        ExperimentFilter filter = new ExperimentFilter(instrument);
        List<Experiment> experiments = experimentService.getAllForUser(user, filter);
        assertEquals(2, experiments.size());
    }

    @Test
    @DisplayName("Count all experiments for a given user and instrument")
    void testCountAllForUserAndInstrument() {
        Instrument instrument = instrumentService.getById(1L);
        User user = userService.getById("1");
        ExperimentFilter filter = new ExperimentFilter(instrument);
        Long experiments = experimentService.getAllCountForUser(user, filter);
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
        QueryFilter filter = new QueryFilter("instrument.id = :id");
        filter.addParameter("id", "1");
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
