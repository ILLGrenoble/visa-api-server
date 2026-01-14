package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.BookingToken;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("BookingToken")
public class BookingTokenType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String uid;
    private final @NotNull FlavourType flavour;
    private final UserType owner;
    private final InstanceType instance;

    public BookingTokenType(BookingToken bookingToken) {
        this.id = bookingToken.getId();
        this.uid = bookingToken.getUid();
        this.flavour = new FlavourType(bookingToken.getFlavour());
        this.owner = bookingToken.getOwner() == null ? null : new UserType(bookingToken.getOwner());
        this.instance = bookingToken.getInstance() == null ? null : new InstanceType(bookingToken.getInstance());
    }

    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public FlavourType getFlavour() {
        return flavour;
    }

    public UserType getOwner() {
        return owner;
    }

    public InstanceType getInstance() {
        return instance;
    }
}
