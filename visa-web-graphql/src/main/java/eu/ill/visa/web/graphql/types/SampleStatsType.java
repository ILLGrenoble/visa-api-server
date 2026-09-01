package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.domain.SampleStats;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("SampleStats")
public record SampleStatsType(
    @AdaptToScalar(Scalar.Float.class)
    Double mean,
    @AdaptToScalar(Scalar.Float.class)
    Double standardDeviation,
    @NotNull @AdaptToScalar(Scalar.Int.class)
    long sampleCount,
    @NotNull Date firstSampleDate) {

    public SampleStatsType(SampleStats sampleStats) {
        this(sampleStats.mean(), sampleStats.standardDeviation(), sampleStats.sampleCount(), sampleStats.firstSampleDate());
    }
}
