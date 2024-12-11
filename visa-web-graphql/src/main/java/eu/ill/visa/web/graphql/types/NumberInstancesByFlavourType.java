package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.partial.NumberInstancesByFlavour;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("NumberInstancesByFlavour")
public class NumberInstancesByFlavourType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long total ;

    public NumberInstancesByFlavourType(final NumberInstancesByFlavour numberInstancesByFlavour) {
        this.id = numberInstancesByFlavour.getId();
        this.name = numberInstancesByFlavour.getName();
        this.total = numberInstancesByFlavour.getTotal();
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
