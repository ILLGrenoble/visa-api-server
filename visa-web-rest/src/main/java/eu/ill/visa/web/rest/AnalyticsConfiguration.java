package eu.ill.visa.web.rest;

import java.util.Optional;

public interface AnalyticsConfiguration {

    Boolean enabled();

    Optional<String> url();

    Optional<Integer> siteId();

}
