package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.partial.NumberInstancesByImage;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("NumberInstancesByImage")
public class NumberInstancesByImageType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    private final String version;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long total ;

    public NumberInstancesByImageType(final NumberInstancesByImage numberInstancesByImage) {
        this.id = numberInstancesByImage.getId();
        this.name = numberInstancesByImage.getName();
        this.version = numberInstancesByImage.getVersion();
        this.total = numberInstancesByImage.getTotal();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Long getTotal() {
        return total;
    }

}
