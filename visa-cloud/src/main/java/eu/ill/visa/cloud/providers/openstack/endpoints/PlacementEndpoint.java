package eu.ill.visa.cloud.providers.openstack.endpoints;

import eu.ill.visa.cloud.domain.CloudResourceInventory;
import eu.ill.visa.cloud.domain.CloudResourceProvider;
import eu.ill.visa.cloud.domain.CloudResourceUsage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudUnavailableException;

import java.util.List;

public interface PlacementEndpoint {
    List<CloudResourceProvider> resourceProviders() throws CloudException, CloudUnavailableException;
    List<CloudResourceInventory> resourceInventories(final String resourceProviderId) throws CloudException, CloudUnavailableException;
    List<CloudResourceUsage> resourceUsages(final String resourceProviderId) throws CloudException, CloudUnavailableException;
}
