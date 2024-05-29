package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.FlavourLimit;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

@Type("FlavourLimit")
public class FlavourLimitType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    @AdaptToScalar(Scalar.Int.class)
    private final Long objectId;
    private final String objectType;
    private final FlavourType flavour;

    public FlavourLimitType(final FlavourLimit flavourLimit) {
        this.id = flavourLimit.getId();
        this.objectId = flavourLimit.getObjectId();
        this.objectType = flavourLimit.getObjectType();
        this.flavour = new FlavourType(flavourLimit.getFlavour());
    }

    public Long getId() {
        return id;
    }

    public Long getObjectId() {
        return objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public FlavourType getFlavour() {
        return flavour;
    }
}
