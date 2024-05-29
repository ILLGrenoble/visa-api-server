package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.domain.NumberInstancesByFlavour;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

@Type("NumberInstancesByFlavour")
public class NumberInstancesByFlavourType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;
    @AdaptToScalar(Scalar.Int.class)
    private final Long total ;

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
