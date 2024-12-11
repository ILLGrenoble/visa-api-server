package eu.ill.visa.cloud.providers.web.converters;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.cloud.domain.CloudInstanceFault;
import eu.ill.visa.cloud.domain.CloudInstanceMetadata;
import eu.ill.visa.cloud.domain.CloudInstanceState;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;
import java.util.List;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudInstanceMixin {

    public CloudInstanceState state;
    public String id;
    public String name;
    public String address;
    public String imageId;
    public @JsonProperty("flavourId") String flavorId;
    public Date createdAt;
    public CloudInstanceFault fault;
    public List<String> securityGroups;
    public CloudInstanceMetadata metadata;
    public String bootCommand;
}
