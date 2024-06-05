package eu.ill.visa.cloud.providers.openstack.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudClientException;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import eu.ill.visa.cloud.providers.openstack.domain.AuthenticationToken;
import eu.ill.visa.cloud.providers.openstack.http.requests.AuthenticationRequest;
import eu.ill.visa.cloud.providers.openstack.http.responses.AuthenticationResponse;
import eu.ill.visa.cloud.providers.openstack.http.IdentityEndpointClient;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OpenStackIdentityEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(OpenStackIdentityEndpoint.class);

    private static final String HEADER_X_SUBJECT_TOKEN = "X-Subject-Token";

    private final IdentityEndpointClient identityEndpointClient;
    private final AuthenticationRequest authenticationRequest;
    private final ObjectMapper mapper;

    private AuthenticationToken token = new AuthenticationToken(null, new Date());

    public OpenStackIdentityEndpoint(final OpenStackProviderConfiguration configuration) {
        this.identityEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getIdentityEndpoint()))
            .build(IdentityEndpointClient.class);
        this.authenticationRequest = new AuthenticationRequest(configuration.getApplicationId(), configuration.getApplicationSecret());
        this.mapper = new ObjectMapper()
            .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public String authenticate() throws CloudException {
        return this.authenticate(false);
    }

    public String authenticate(boolean force) throws CloudException  {
        if (force || !this.token.isValid()) {
            try (Response response = this.identityEndpointClient.authenticate(this.authenticationRequest)) {
                String token = this.getSubjectToken(response);
                String body = response.readEntity(String.class);
                Date expiresAt = new Date();
                try {
                    AuthenticationResponse authenticationResponse = this.mapper.readValue(body, AuthenticationResponse.class);
                    expiresAt = new Date(expiresAt.getTime() + authenticationResponse.getHalfDurationMs());
                } catch (JsonProcessingException e) {
                    logger.warn("Failed to deserialize OpenStack Authentication Token: {}", e.getMessage());
                }

                this.token = new AuthenticationToken(token, expiresAt);

            } catch (CloudClientException e) {
                throw new CloudException(e.getMessage());
            }
        }
        return this.token.token();
    }

    private String getSubjectToken(final Response response) {
        for (Map.Entry<String, List<String>> entry : response.getStringHeaders().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(HEADER_X_SUBJECT_TOKEN)) {
                return entry.getValue().getFirst();
            }
        }
        return null;
    }
}
