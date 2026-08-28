package eu.ill.visa.vdi.domain.models;


import eu.ill.visa.core.domain.SampleStats;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RttSampler {

    private final SampleCalculator clientRttSampler = new SampleCalculator();
    private final SampleCalculator instanceRttSampler = new SampleCalculator();

    public synchronized void addClientRttSample(long rttMs) {
        this.clientRttSampler.addValue(rttMs);
    }

    public synchronized void addInstanceRttSample(long rttMs) {
        this.instanceRttSampler.addValue(rttMs);
    }

    public synchronized Pair<SampleStats, SampleStats> calculateRttSampleStats() {
        return new ImmutablePair<>(this.clientRttSampler.calculateStats(), this.instanceRttSampler.calculateStats());
    }

    public synchronized void reset() {
        this.clientRttSampler.reset();
        this.instanceRttSampler.reset();
    }

    private static final class SampleCalculator {
        private final List<Long> values = new ArrayList<>();
        private Date firstSampleDate;

        public void addValue(long value) {
            if (this.firstSampleDate == null) {
                this.firstSampleDate = new Date();
            }
            values.add(value);
        }

        public void reset() {
            values.clear();
            this.firstSampleDate = null;
        }

        public SampleStats calculateStats() {
            if (values.isEmpty()) {
                return new SampleStats(null, null, 0, new Date());

            } else if (values.size() == 1) {
                return new SampleStats((double)values.getFirst(), 0d, 1, this.firstSampleDate);

            } else {
                double mean = values.stream()
                    .mapToDouble(v -> (double)v)
                    .sum() / values.size();

                double standardDeviation = Math.sqrt(values.stream()
                    .map(v -> (double)v - mean)
                    .map(v -> v * v)
                    .mapToDouble(v -> v)
                    .sum() / (values.size() - 1));

                return new SampleStats(mean, standardDeviation, values.size(), firstSampleDate);
            }

        }
    }
}
