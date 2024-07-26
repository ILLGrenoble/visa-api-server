package eu.ill.visa.broker;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "broker", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface MessageBrokerConfiguration {

    boolean redisEnabled();

    Optional<String> redisURL();

    Optional<String> redisPassword();

    Integer redisDatabase();
}
