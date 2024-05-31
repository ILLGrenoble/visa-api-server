package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudRuntimeException;
import eu.ill.visa.cloud.providers.openstack.domain.Authentication;
import eu.ill.visa.cloud.providers.openstack.http.IdentityEndpointClient;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class OpenStackIdentityProvider {

    private static final String HEADER_X_SUBJECT_TOKEN = "X-Subject-Token";

    private final IdentityEndpointClient identityEndpointClient;
    private final Authentication authentication;

    public OpenStackIdentityProvider(final OpenStackProviderConfiguration configuration) {
        this.identityEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getIdentityEndpoint()))
            .build(IdentityEndpointClient.class);
        this.authentication = new Authentication(configuration.getApplicationId(), configuration.getApplicationSecret());
    }

    public String authenticate() throws CloudException  {
        try (Response response = this.identityEndpointClient.authenticate(this.authentication)) {
            return this.getSubjectToken(response);
        } catch (CloudRuntimeException e) {
            throw new CloudException(e.getMessage());
        }
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
