package eu.ill.visa.cloud.providers.openstack.http.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ResourceUsagesResponse(@JsonProperty("usages") Map<String, Long> resourceUsages) {
}
