package eu.ill.visa.cloud.providers.openstack.http.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StopInstanceActionRequest extends InstanceActionRequest {
    @JsonProperty("os-stop")
    public String stop = null;
}
