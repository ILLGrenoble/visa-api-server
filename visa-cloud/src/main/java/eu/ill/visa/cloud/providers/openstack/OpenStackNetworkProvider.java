package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.openstack.http.NetworkEndpointClient;
import eu.ill.visa.cloud.providers.openstack.http.responses.SecurityGroupsResponse;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OpenStackNetworkProvider extends AuthenticatedOpenStackProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackNetworkProvider.class);

    private final NetworkEndpointClient networkEndpointClient;

    public OpenStackNetworkProvider(final OpenStackProviderConfiguration configuration,
                                    final OpenStackIdentityProvider identityProvider) {
        super(identityProvider);
        this.networkEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getNetworkEndpoint()))
            .build(NetworkEndpointClient.class);
    }

    public List<String> securityGroups() throws CloudException {
        try {
            return this.networkEndpointClient.securityGroups(this.authenticate()).securityGroups().stream()
                .map(SecurityGroupsResponse.SecurityGroup::name)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.networkEndpointClient.securityGroups(this.authenticate(true)).securityGroups().stream()
                .map(SecurityGroupsResponse.SecurityGroup::name)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.error("Failed to get security groups from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
