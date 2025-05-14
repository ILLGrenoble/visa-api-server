package eu.ill.visa.business;

public interface MetricsConfiguration {

    Boolean enabled();
    String hostname();
    Long exportIntervalSeconds();
}
