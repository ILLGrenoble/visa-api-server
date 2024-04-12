package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "business.signature")
public interface SignatureConfiguration {

    Optional<String> privateKeyPath();
    Optional<String> publicKeyPath();
}
