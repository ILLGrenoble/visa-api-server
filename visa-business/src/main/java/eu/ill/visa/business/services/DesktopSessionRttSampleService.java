package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.SampleStats;
import eu.ill.visa.core.entity.DesktopSessionRttSample;
import eu.ill.visa.persistence.repositories.DesktopSessionRttSampleRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Singleton
public class DesktopSessionRttSampleService {

    private static final Logger logger = LoggerFactory.getLogger(DesktopSessionRttSampleService.class);

    private final DesktopSessionRttSampleRepository repository;

    @Inject
    public DesktopSessionRttSampleService(final DesktopSessionRttSampleRepository repository) {
        this.repository = repository;
    }

    public List<DesktopSessionRttSample> getByInstanceSessionMemberId(Long instanceSessionMemberId) {
        return this.repository.getByInstanceSessionMemberId(instanceSessionMemberId);
    }

    public List<List<DesktopSessionRttSample>> getByInstanceSessionMemberIds(List<Long> instanceSessionMemberIds) {
        List<DesktopSessionRttSample> samples = this.repository.getByInstanceSessionMemberIds(instanceSessionMemberIds);
        return instanceSessionMemberIds.stream().map(id -> {
            return samples.stream().filter(sample -> sample.getInstanceSessionMemberId().equals(id)).toList();
        }).toList();
    }

    public List<DesktopSessionRttSample> getByInstanceId(Long instanceId) {
        return this.repository.getByInstanceId(instanceId);
    }

    public List<DesktopSessionRttSample> getAllRequiringResampling(int daysOld) {
        return this.repository.getAllRequiringResampling(daysOld);
    }

    public void save(DesktopSessionRttSample desktopSessionRttSample) {
        this.repository.save(desktopSessionRttSample);
    }

    public Map<HourlySampleKey, List<DesktopSessionRttSample>> groupSamples(List<DesktopSessionRttSample> samples) {
        Map<HourlySampleKey, List<DesktopSessionRttSample>> grouped =
            samples.stream()
                .collect(Collectors.groupingBy(
                    sample -> new HourlySampleKey(
                        sample.getInstanceSessionMemberId(),
                        getHourlyBucket(sample.getDate())
                    )
                ));
        return grouped;
    }

    public SampleStats mergeClientStats(List<DesktopSessionRttSample> samples, Instant hour) {
        SampleStats clientStats = samples.stream()
            .map(sample -> SampleStats.fromSample(
                sample.getClientMeanRttMs(),
                sample.getClientSdRttMs(),
                sample.getClientRttSampleCount(),
                sample.getDate()
            ))
            .reduce(SampleStats::merge)
            .orElse(SampleStats.fromSample(null, null, 0, Date.from(hour)));

        return clientStats;
    }

    public SampleStats mergeInstanceStats(List<DesktopSessionRttSample> samples, Instant hour) {
        SampleStats instanceStats = samples.stream()
            .map(sample -> SampleStats.fromSample(
                sample.getInstanceMeanRttMs(),
                sample.getInstanceSdRttMs(),
                sample.getInstanceRttSampleCount(),
                sample.getDate()
            ))
            .reduce(SampleStats::merge)
            .orElse(SampleStats.fromSample(null, null, 0, Date.from(hour)));

        return instanceStats;
    }

    public Pair<Integer, Integer> performResampling(int daysOld) {
        // Get all samples to be resampled
        List<DesktopSessionRttSample> samplesToReduce = this.getAllRequiringResampling(daysOld);

        // Group samples by instanceSessionMemberId and truncated hour
        Map<HourlySampleKey, List<DesktopSessionRttSample>> grouped = this.groupSamples(samplesToReduce);

        List<DesktopSessionRttSample> hourlySamples = new ArrayList<>();

        // Calculate new means and standard deviations for hourly binned samples
        for (Map.Entry<HourlySampleKey, List<DesktopSessionRttSample>>
            entry : grouped.entrySet()) {

            HourlySampleKey key = entry.getKey();
            List<DesktopSessionRttSample> samples = entry.getValue();

            Long instanceSessionMemberId = key.instanceSessionMemberId;
            Instant hour = key.hour;

            // calculate clientStats
            SampleStats clientStats = this.mergeClientStats(samples, hour);

            // calculate instanceStats
            SampleStats instanceStats = this.mergeInstanceStats(samples, hour);

            DesktopSessionRttSample hourlySample = DesktopSessionRttSample.Builder()
                .instanceSessionMemberId(instanceSessionMemberId)
                .date(Date.from(hour))
                .samplePeriodMinutes(60L)
                .clientMeanRttMs(clientStats.mean())
                .clientSdRttMs(clientStats.standardDeviation())
                .clientRttSampleCount(clientStats.sampleCount())
                .instanceMeanRttMs(instanceStats.mean())
                .instanceSdRttMs(instanceStats.standardDeviation())
                .instanceRttSampleCount(instanceStats.sampleCount())
                .build();

            hourlySamples.add(hourlySample);
        }

        // Store the hourly aggregates.
        for (DesktopSessionRttSample hourlySample : hourlySamples) {
            this.repository.save(hourlySample);
        }

        // Delete old samples
        this.repository.deleteAll(samplesToReduce);

        // Return reduction in number of samples
        return new ImmutablePair<>(samplesToReduce.size(), hourlySamples.size());
    }

    private Instant getHourlyBucket(Date date) {
        return date.toInstant().truncatedTo(ChronoUnit.HOURS);
    }

    public record HourlySampleKey(Long instanceSessionMemberId, Instant hour) {}
}
