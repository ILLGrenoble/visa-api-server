package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.Instrument;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

@Type("Instrument")
public class InstrumentType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;

    public InstrumentType(final Instrument instrument) {
        this.id = instrument.getId();
        this.name = instrument.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
