package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudImage;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import static java.lang.Math.toIntExact;

@Type("CloudImage")
public class CloudImageType {

    private final @NotNull String id;
    private final @NotNull String name;
    private final @NotNull Integer size;

    public CloudImageType(final CloudImage cloudImage) {
        this.id = cloudImage.getId();
        this.name = cloudImage.getName();
        this.size = toIntExact(cloudImage.getSize() / 1024 / 1024);
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
}
