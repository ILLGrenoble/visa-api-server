package eu.ill.visa.core.domain.metrics;


import java.util.Date;

public record HistogramRecord<T>(T value, String attribute, Date timestamp) {
    public HistogramRecord(T value) {
        this(value, null, new Date());
    }
    public HistogramRecord(T value, String attribute) {
        this(value, attribute, new Date());
    }
}
