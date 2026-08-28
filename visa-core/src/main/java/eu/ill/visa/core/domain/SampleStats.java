package eu.ill.visa.core.domain;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Date;

public record SampleStats(Double mean, Double standardDeviation, long sampleCount, Date firstSampleDate) {
    private final static Logger logger = LoggerFactory.getLogger(SampleStats.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("00.000");

    public static SampleStats merge(SampleStats a, SampleStats b) {
        if (a.sampleCount == 0) {
            return b;
        }

        if (b.sampleCount == 0) {
            return a;
        }

        long n1 = a.sampleCount;
        long n2 = b.sampleCount;

        long n = n1 + n2;

        double mean1 = a.mean;
        double mean2 = b.mean;

        double sd1 = a.standardDeviation != null ? a.standardDeviation : 0.0;
        double sd2 = b.standardDeviation != null ? b.standardDeviation : 0.0;

        double m2_1 = n1 > 1 ? (n1 - 1) * sd1 * sd1 : 0.0;
        double m2_2 = n2 > 1 ? (n2 - 1) * sd2 * sd2 : 0.0;

        double delta = mean2 - mean1;

        double mean = (mean1 * n1 + mean2 * n2) / (double) n;

        double m2 = m2_1 + m2_2 + delta * delta * n1 * n2 / (double) n;

        double variance = n > 1 ? m2 / (n - 1) : 0.0;

        double sd = Math.sqrt(variance);

        //logger.info("n_1 = {}\tmu_1 = {}\tsd_1 = {}\t|\tn_2 = {}\tmu_2 = {}\tsd_2 = {}\t|\tn = {}\tmu = {}\tsd = {}", n1, DECIMAL_FORMAT.format(mean1), DECIMAL_FORMAT.format(sd1), n2, DECIMAL_FORMAT.format(mean2), DECIMAL_FORMAT.format(sd2), n, DECIMAL_FORMAT.format(mean), DECIMAL_FORMAT.format(sd));

        Date earliestDate = a.firstSampleDate.before(b.firstSampleDate) ? a.firstSampleDate : b.firstSampleDate;

        return new SampleStats(mean, Math.sqrt(variance), n, earliestDate);
    }

    public static SampleStats fromSample(Double mean, Double standardDeviation, long sampleCount, Date date) {
        if (sampleCount == 0 || mean == null) {
            return new SampleStats(null, null, 0, date);
        }

        return new SampleStats(mean, standardDeviation, sampleCount, date);
    }

}
