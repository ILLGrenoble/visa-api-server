package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceExtensionRequest;
import eu.ill.visa.core.entity.enumerations.InstanceExtensionRequestState;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("InstanceExtensionRequest")
public class InstanceExtensionRequestType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull InstanceType instance;
    private final @NotNull String comments;
    private final @NotNull Date createdAt;
    private final @NotNull InstanceExtensionRequestState state;

    public InstanceExtensionRequestType(final InstanceExtensionRequest request) {
        this.id = request.getId();
        this.instance = new InstanceType(request.getInstance());
        this.comments = request.getComments();
        this.createdAt = request.getCreatedAt();
        this.state = request.getState();
    }

    public Long getId() {
        return id;
    }

    public InstanceType getInstance() {
        return instance;
    }

    public String getComments() {
        return comments;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public InstanceExtensionRequestState getState() {
        return state;
    }
}
