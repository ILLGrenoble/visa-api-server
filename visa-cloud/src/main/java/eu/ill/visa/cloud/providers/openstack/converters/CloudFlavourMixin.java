package eu.ill.visa.cloud.providers.openstack.converters;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CloudFlavourMixin {

    public String id;
    public String name;
    public @JsonProperty("vcpus") Integer cpus;
    public Integer ram;
    public Integer disk;

}
