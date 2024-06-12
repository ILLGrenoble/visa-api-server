package eu.ill.visa.business;

import java.util.List;
import java.util.Optional;

public interface ErrorReportEmailConfiguration {

    boolean enabled();

    Optional<List<String>> to();

    Optional<String> from();

    Optional<String> subject();

    int maxErrors();
}
