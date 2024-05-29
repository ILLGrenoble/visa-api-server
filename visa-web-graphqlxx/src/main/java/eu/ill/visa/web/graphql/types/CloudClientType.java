package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.services.CloudClient;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("CloudClient")
public class CloudClientType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    private final @NotNull String type;
    private final @NotNull String serverNamePrefix;
    private final @NotNull Boolean visible;

    public CloudClientType(final CloudClient cloudClient) {
        this.id = cloudClient.getId();
        this.name = cloudClient.getName();
        this.type = cloudClient.getType();
        this.serverNamePrefix = cloudClient.getServerNamePrefix();
        this.visible = cloudClient.getVisible();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getServerNamePrefix() {
        return serverNamePrefix;
    }

    public Boolean getVisible() {
        return visible;
    }
}
