package eu.ill.visa.cloud.providers.openstack.endpoints;

import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import eu.ill.visa.cloud.providers.openstack.http.NetworkEndpointClient;
import eu.ill.visa.cloud.providers.openstack.http.responses.SecurityGroupsResponse;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OpenStackNetworkEndpoint implements NetworkEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackNetworkEndpoint.class);

    private final OpenStackIdentityEndpoint identityProvider;
    private final NetworkEndpointClient networkEndpointClient;

    private OpenStackNetworkEndpoint(final OpenStackProviderConfiguration configuration,
                                    final OpenStackIdentityEndpoint identityProvider) {
        this.identityProvider = identityProvider;
        this.networkEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getNetworkEndpoint()))
            .build(NetworkEndpointClient.class);
    }
    public static NetworkEndpoint authenticationProxy(final OpenStackProviderConfiguration configuration,
                                                    final OpenStackIdentityEndpoint identityEndpoint) {
        final OpenStackNetworkEndpoint openStackNetworkEndpoint = new OpenStackNetworkEndpoint(configuration, identityEndpoint);
        return (NetworkEndpoint) Proxy.newProxyInstance(
            openStackNetworkEndpoint.getClass().getClassLoader(),
            new Class[] { NetworkEndpoint.class }, (target, method, methodArgs) -> {
                try {
                    return method.invoke(openStackNetworkEndpoint, methodArgs);

                } catch (CloudAuthenticationException e) {
                    identityEndpoint.authenticate(true);

                    try {
                        return method.invoke(openStackNetworkEndpoint, methodArgs);

                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }

                } catch (InvocationTargetException ex) {
                    throw ex.getCause();
                }
            });
    }
    public List<String> securityGroups() throws CloudException {
        try {
            return this.networkEndpointClient.securityGroups(this.identityProvider.authenticate()).securityGroups().stream()
                .map(SecurityGroupsResponse.SecurityGroup::name)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        } catch (Exception e) {
            logger.error("Failed to get security groups from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
