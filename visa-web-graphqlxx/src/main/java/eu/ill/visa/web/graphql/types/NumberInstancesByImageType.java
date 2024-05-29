package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.domain.NumberInstancesByImage;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

@Type("NumberInstancesByImage")
public class NumberInstancesByImageType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;
    private final String version;
    @AdaptToScalar(Scalar.Int.class)
    private final Long total ;

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
