package eu.ill.visa.cloud.providers.openstack.http.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.cloud.domain.CloudResourceProvider;

import java.util.List;

public record ResourceProvidersResponse(@JsonProperty("resource_providers") List<CloudResourceProvider> resourceProviders) {
}
