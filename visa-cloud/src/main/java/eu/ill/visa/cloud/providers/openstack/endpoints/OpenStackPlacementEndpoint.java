package eu.ill.visa.cloud.providers.openstack.endpoints;

import eu.ill.visa.cloud.CloudConfiguration;
import eu.ill.visa.cloud.domain.CloudResourceClass;
import eu.ill.visa.cloud.domain.CloudResourceInventory;
import eu.ill.visa.cloud.domain.CloudResourceProvider;
import eu.ill.visa.cloud.domain.CloudResourceUsage;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudClientException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudUnavailableException;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import eu.ill.visa.cloud.providers.openstack.http.PlacementEndpointClient;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OpenStackPlacementEndpoint implements PlacementEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackPlacementEndpoint.class);

    private final OpenStackIdentityEndpoint identityProvider;
    private final PlacementEndpointClient placementEndpointClient;

    private OpenStackPlacementEndpoint(final CloudConfiguration cloudConfiguration,
                                       final OpenStackProviderConfiguration openStackConfiguration,
                                       final OpenStackIdentityEndpoint identityProvider) {

        this.identityProvider = identityProvider;
        this.placementEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(openStackConfiguration.getPlacementEndpoint()))
            .readTimeout(cloudConfiguration.restClientReadTimeoutMs(), TimeUnit.MILLISECONDS)
            .connectTimeout(cloudConfiguration.restClientConnectTimeoutMs(), TimeUnit.MILLISECONDS)
            .build(PlacementEndpointClient.class);
    }

    public static PlacementEndpoint authenticationProxy(final String name,
                                                        final CloudConfiguration cloudConfiguration,
                                                        final OpenStackProviderConfiguration openStackConfiguration,
                                                        final OpenStackIdentityEndpoint identityEndpoint) {
        if (openStackConfiguration.getPlacementEndpoint() == null) {
            logger.info("OpenStack Placement Endpoint is not configured for provider {}", name);
            return (PlacementEndpoint) Proxy.newProxyInstance(
                OpenStackPlacementEndpoint.class.getClassLoader(),
                new Class[] { PlacementEndpoint.class }, (target, method, methodArgs) -> {
                    throw new CloudUnavailableException("The OpenStack Placement endpoint has not been configured");
                });

        } else {
            logger.info("OpenStack Placement Endpoint is configured for provider {}", name);
            final OpenStackPlacementEndpoint openStackPlacementEndpoint = new OpenStackPlacementEndpoint(cloudConfiguration, openStackConfiguration, identityEndpoint);
            return (PlacementEndpoint) Proxy.newProxyInstance(
                openStackPlacementEndpoint.getClass().getClassLoader(),
                new Class[] { PlacementEndpoint.class }, (target, method, methodArgs) -> {
                    try {
                        return method.invoke(openStackPlacementEndpoint, methodArgs);

                    } catch (CloudAuthenticationException e) {
                        identityEndpoint.authenticate(true);

                        try {
                            return method.invoke(openStackPlacementEndpoint, methodArgs);

                        } catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }

                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }
                });
        }
    }

    public List<CloudResourceProvider> resourceProviders() throws CloudException {
        try {
            return this.placementEndpointClient.resourceProviders(this.identityProvider.authenticate()).resourceProviders();

        } catch (CloudClientException e) {
            logger.error("Failed to get cloud resource providers from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<CloudResourceInventory> resourceInventories(final String resourceProviderId) throws CloudException {
        try {
            return this.placementEndpointClient.resourceInventories(this.identityProvider.authenticate(), resourceProviderId).resourceInventories()
                .entrySet().stream().map(entry -> {
                    String resourceClass =  entry.getKey();
                    CloudResourceInventory resourceInventory = entry.getValue();
                    resourceInventory.setResourceClass(this.toResourceClass(resourceClass));
                    return resourceInventory;
                })
                .toList();

        } catch (CloudClientException e) {
            logger.error("Failed to get cloud resource inventories from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<CloudResourceUsage> resourceUsages(final String resourceProviderId) throws CloudException {
        try {
            return this.placementEndpointClient.resourceUsages(this.identityProvider.authenticate(), resourceProviderId).resourceUsages()
                .entrySet().stream().map(entry -> {
                    String resourceClass =  entry.getKey();
                    Long resourceUsage = entry.getValue();
                    return new CloudResourceUsage(this.toResourceClass(resourceClass), resourceUsage);
                })
                .toList();

        } catch (CloudClientException e) {
            logger.error("Failed to get cloud resource usages from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private CloudResourceClass toResourceClass(final String resourceClassName) {
        if (resourceClassName.equals(CloudResourceClass.VCPU_RESOURCE_CLASS)) {
            return CloudResourceClass.VCPUResourceClass();

        } else if (resourceClassName.equals(CloudResourceClass.MEMORY_MB_RESOURCE_CLASS)) {
            return CloudResourceClass.MemoryMBResourceClass();

        } else if (resourceClassName.equals(CloudResourceClass.DISK_GM_RESOURCE_CLASS)) {
            return CloudResourceClass.DiskGBResourceClass();

        } else {
            return CloudResourceClass.CustomResourceClass(resourceClassName);
        }
    }

}
