package eu.ill.visa.cloud.providers.openstack.http.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ExtraSpecsResponse(@JsonProperty("extra_specs") Map<String, String> extraSpecs) {
}
