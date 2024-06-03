package eu.ill.visa.cloud.providers.openstack.http.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StartInstanceActionRequest extends InstanceActionRequest  {
    @JsonProperty("os-start")
    public String start = null;
}
