package eu.ill.visa.cloud.providers.openstack.http.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.cloud.domain.CloudResourceInventory;

import java.util.Map;

public record ResourceInventoriesResponse(@JsonProperty("inventories") Map<String, CloudResourceInventory> resourceInventories) {
}
