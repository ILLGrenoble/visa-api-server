package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.domain.NumberInstancesByCloudClient;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("NumberInstancesByCloudClient")
public class NumberInstancesByCloudClientType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long total ;

    public NumberInstancesByCloudClientType(final NumberInstancesByCloudClient numberInstancesByCloudClient) {
        this.id = numberInstancesByCloudClient.getId();
        this.name = numberInstancesByCloudClient.getName();
        this.total = numberInstancesByCloudClient.getTotal();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getTotal() {
        return total;
    }

}
