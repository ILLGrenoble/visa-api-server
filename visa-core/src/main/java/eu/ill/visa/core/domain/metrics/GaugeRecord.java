package eu.ill.visa.core.domain.metrics;


import java.util.Date;

record GaugeRecord<T>(T value, Date timestamp) {
    public GaugeRecord(T value) {
        this(value, new Date());
    }
}
