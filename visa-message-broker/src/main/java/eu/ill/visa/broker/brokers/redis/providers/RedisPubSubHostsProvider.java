package eu.ill.visa.broker.brokers.redis.providers;

import eu.ill.visa.broker.MessageBrokerConfiguration;
import io.quarkus.redis.client.RedisHostsProvider;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
@Identifier("redis-pubsub-hosts-provider")
public class RedisPubSubHostsProvider implements RedisHostsProvider {

    private final MessageBrokerConfiguration configuration;

    @ConfigProperty(name = "quarkus.redis.tls.enabled")
    Boolean tlsEnabled;

    @Inject
    public RedisPubSubHostsProvider(final MessageBrokerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Set<URI> getHosts() {
        if (this.configuration.redisEnabled()) {
            final String url = this.configuration.redisURL().orElse(null);
            final Integer database = this.configuration.redisDatabase();
            final String password = this.configuration.redisPassword().orElse(null);
            final String protocol = this.tlsEnabled ? "rediss" : "redis";

            if (url != null && database != null && password != null) {
                URI originalUri = URI.create(url);
                String host = originalUri.getHost();
                int port = originalUri.getPort();

                URI uri = URI.create(String.format("%s://:%s@%s:%d/%d", protocol, URLEncoder.encode(password, StandardCharsets.UTF_8), host, port, database));

                return Collections.singleton(uri);
            }
        }

        return new HashSet<>();
    }
}
