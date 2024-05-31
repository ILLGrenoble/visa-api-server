package eu.ill.visa.cloud.providers.openstack.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CloudImageMixin {

    public String id;
    public String name;
    public Long size;
    @JsonProperty("created_at")
    public String createdAt;

}
