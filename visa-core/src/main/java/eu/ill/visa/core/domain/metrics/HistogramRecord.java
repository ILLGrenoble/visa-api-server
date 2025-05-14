package eu.ill.visa.core.domain.metrics;


import java.util.Date;

record HistogramRecord<T>(T value, String attribute, Date timestamp) {
    HistogramRecord(T value) {
        this(value, null, new Date());
    }
    HistogramRecord(T value, String attribute) {
        this(value, attribute, new Date());
    }
}
