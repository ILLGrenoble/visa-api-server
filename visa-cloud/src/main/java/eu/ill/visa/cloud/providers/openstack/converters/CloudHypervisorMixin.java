package eu.ill.visa.cloud.providers.openstack.converters;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class CloudHypervisorMixin {

    private String id;
    private @JsonProperty("hypervisor_hostname") String hostname;
    private String state;
    private String status;
}
