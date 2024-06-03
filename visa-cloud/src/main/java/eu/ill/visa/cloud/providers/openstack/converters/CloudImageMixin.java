package eu.ill.visa.cloud.providers.openstack.converters;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CloudImageMixin {

    public String id;
    public String name;
    public Long size;
    public @JsonProperty("created_at") String createdAt;

}
