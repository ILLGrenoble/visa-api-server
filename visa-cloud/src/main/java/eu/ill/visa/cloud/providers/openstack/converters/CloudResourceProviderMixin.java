package eu.ill.visa.cloud.providers.openstack.converters;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class CloudResourceProviderMixin {

    private String uuid;
    private @JsonProperty("parent_provider_uuid") String parentUuid;
    private String name;
}
