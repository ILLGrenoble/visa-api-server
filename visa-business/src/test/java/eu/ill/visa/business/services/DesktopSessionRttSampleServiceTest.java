package eu.ill.visa.business.services;

import eu.ill.visa.business.services.DesktopSessionRttSampleService.HourlySampleKey;
import eu.ill.visa.core.domain.SampleStats;
import eu.ill.visa.core.entity.DesktopSessionRttSample;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestTransaction
public class DesktopSessionRttSampleServiceTest {

    @Inject
    private DesktopSessionRttSampleService desktopSessionRttSampleService;

    @Test
    @DisplayName("Test get by instance session member Id")
    void testGetByInstanceSessionMemberId() {
        List<DesktopSessionRttSample> samples = this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11700L);
        assertEquals(10, samples.size());
    }

    @Test
    @DisplayName("Test get all requiring resampling")
    void testGetAllRequiringReduction() {
        List<DesktopSessionRttSample> samples = this.desktopSessionRttSampleService.getAllRequiringResampling(7);
        assertEquals(19, samples.size());
    }

    @Test
    @DisplayName("Test grouping of samples")
    void testGroupingOfSamples() {
        List<DesktopSessionRttSample> samples = this.desktopSessionRttSampleService.getAllRequiringResampling(7);

        Map<HourlySampleKey, List<DesktopSessionRttSample>> grouped = this.desktopSessionRttSampleService.groupSamples(samples);
        assertEquals(5, grouped.size());

        for (List<DesktopSessionRttSample> samplesList : grouped.values()) {
            if (samplesList.get(0).getInstanceSessionMemberId().equals(11700L)) {
                assertEquals(5, samplesList.size());
            } else if (samplesList.get(0).getInstanceSessionMemberId().equals(11701L)) {
                assertEquals(5, samplesList.size());
            } else if (samplesList.get(0).getInstanceSessionMemberId().equals(11702L)) {
                assertEquals(2, samplesList.size());
            } else if (samplesList.get(0).getInstanceSessionMemberId().equals(11703L)) {
                assertEquals(2, samplesList.size());
            }
        }
    }

    @Test
    @DisplayName("Test reduction of client samples")
    void testReductionOfClientSamples() {
        List<DesktopSessionRttSample> samples = this.desktopSessionRttSampleService.getAllRequiringResampling(7);

        Map<HourlySampleKey, List<DesktopSessionRttSample>> grouped = this.desktopSessionRttSampleService.groupSamples(samples);
        Map.Entry<HourlySampleKey, List<DesktopSessionRttSample>> testSamplesEntry = grouped.entrySet().stream()
            .filter(entry -> entry.getKey().instanceSessionMemberId().equals(11702L))
            .findFirst()
            .orElse(null);

        assertNotNull(testSamplesEntry);
        HourlySampleKey key = testSamplesEntry.getKey();
        List<DesktopSessionRttSample> testSamples = testSamplesEntry.getValue();

        assertEquals(2, testSamples.size());
        SampleStats clientStats = this.desktopSessionRttSampleService.mergeClientStats(testSamples, key.hour());

        assertEquals(8, clientStats.sampleCount());
        assertEquals(13.875, clientStats.mean());
        assertTrue(Math.abs(clientStats.standardDeviation() - 1.620) < 0.001);
    }

    @Test
    @DisplayName("Test reduction of instance samples")
    void testReductionOfInstanceSamples() {
        List<DesktopSessionRttSample> samples = this.desktopSessionRttSampleService.getAllRequiringResampling(7);

        Map<HourlySampleKey, List<DesktopSessionRttSample>> grouped = this.desktopSessionRttSampleService.groupSamples(samples);
        Map.Entry<HourlySampleKey, List<DesktopSessionRttSample>> testSamplesEntry = grouped.entrySet().stream()
            .filter(entry -> entry.getKey().instanceSessionMemberId().equals(11702L))
            .findFirst()
            .orElse(null);

        assertNotNull(testSamplesEntry);
        HourlySampleKey key = testSamplesEntry.getKey();
        List<DesktopSessionRttSample> testSamples = testSamplesEntry.getValue();

        assertEquals(2, testSamples.size());
        SampleStats instanceStats = this.desktopSessionRttSampleService.mergeInstanceStats(testSamples, key.hour());

        assertEquals(8, instanceStats.sampleCount());
        assertEquals(2.5625, instanceStats.mean());
        assertTrue(Math.abs(instanceStats.standardDeviation() - 1.412) < 0.001);
    }

    @Test
    @DisplayName("Test reduction of null samples")
    void testReductionOfNullSamples() {
        List<DesktopSessionRttSample> samples = this.desktopSessionRttSampleService.getAllRequiringResampling(7);

        Map<HourlySampleKey, List<DesktopSessionRttSample>> grouped = this.desktopSessionRttSampleService.groupSamples(samples);
        Map.Entry<HourlySampleKey, List<DesktopSessionRttSample>> testSamplesEntry = grouped.entrySet().stream()
            .filter(entry -> entry.getKey().instanceSessionMemberId().equals(11703L))
            .findFirst()
            .orElse(null);

        assertNotNull(testSamplesEntry);
        HourlySampleKey key = testSamplesEntry.getKey();
        List<DesktopSessionRttSample> testSamples = testSamplesEntry.getValue();

        assertEquals(2, testSamples.size());
        SampleStats clientStats = this.desktopSessionRttSampleService.mergeClientStats(testSamples, key.hour());

        assertEquals(5, clientStats.sampleCount());
        assertEquals(13.5, clientStats.mean());
        assertTrue(Math.abs(clientStats.standardDeviation() - 2.000) < 0.001);
    }

    @Test
    @DisplayName("Test full reduction")
    void testFullReduction() {
        List<DesktopSessionRttSample> samplesBefore = this.desktopSessionRttSampleService.getAllRequiringResampling(7);
        assertEquals(19, samplesBefore.size());

        assertEquals(10, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11700L).size());
        assertEquals(5, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11701L).size());
        assertEquals(2, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11702L).size());
        assertEquals(2, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11703L).size());
        assertEquals(5, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11800L).size());

        Pair<Integer, Integer> results = this.desktopSessionRttSampleService.performResampling(7);
        assertEquals(19, results.getLeft());
        assertEquals(5, results.getRight());

        List<DesktopSessionRttSample> samplesAfter = this.desktopSessionRttSampleService.getAllRequiringResampling(7);
        assertEquals(0, samplesAfter.size());

        assertEquals(2, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11700L).size());
        assertEquals(1, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11701L).size());
        assertEquals(1, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11702L).size());
        assertEquals(1, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11703L).size());
        assertEquals(5, this.desktopSessionRttSampleService.getByInstanceSessionMemberId(11800L).size());
    }

}
