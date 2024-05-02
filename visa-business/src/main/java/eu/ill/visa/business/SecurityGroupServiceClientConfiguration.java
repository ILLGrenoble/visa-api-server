package eu.ill.visa.business;

import java.util.Optional;

public interface SecurityGroupServiceClientConfiguration {

    Boolean enabled();
    Optional<String> url();
    Optional<String> authToken();
}

