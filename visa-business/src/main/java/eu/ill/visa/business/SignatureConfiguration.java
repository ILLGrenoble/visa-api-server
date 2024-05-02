package eu.ill.visa.business;

import java.util.Optional;

public interface SignatureConfiguration {

    Optional<String> privateKeyPath();
    Optional<String> publicKeyPath();
}
