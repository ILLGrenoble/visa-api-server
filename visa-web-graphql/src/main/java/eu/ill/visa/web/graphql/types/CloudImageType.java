package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudImage;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import static java.lang.Math.toIntExact;

@Type("CloudImage")
public class CloudImageType {

    private final @NotNull String id;
    private final @NotNull String name;
    private final @NotNull Integer size;
    @AdaptToScalar(Scalar.DateTime.class)
    private final @NotNull String createdAt;

    public CloudImageType(final CloudImage cloudImage) {
        this.id = cloudImage.getId();
        this.name = cloudImage.getName();
        this.size = toIntExact(cloudImage.getSize() / 1024 / 1024);
        this.createdAt = cloudImage.getCreatedAt();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getSize() {
        return size;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
