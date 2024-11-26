package eu.ill.visa.web.graphql.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("Instance")
public class InstanceType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String uid;
    private final @NotNull String name;
    private final String ipAddress;
    private final String comments;
    private final @NotNull InstanceState state;
    private final PlanType plan;
    private final @NotNull Date createdAt;
    private final Date lastSeenAt;
    private final Date lastInteractionAt;
    private final Date terminationDate;
    private final @NotNull String username;
    private final @NotNull String keyboardLayout;
    private final Long cloudId;
    private final String computeId;

    public InstanceType(final Instance instance) {
        this.id = instance.getId();
        this.uid = instance.getUid();
        this.name = instance.getName();
        this.ipAddress = instance.getIpAddress();
        this.comments = instance.getComments();
        this.state = instance.getState();
        this.plan = new PlanType(instance.getPlan());
        this.createdAt = instance.getCreatedAt();
        this.lastSeenAt = instance.getLastSeenAt();
        this.lastInteractionAt = instance.getLastInteractionAt();
        this.terminationDate = instance.getTerminationDate();
        this.username = instance.getUsername();
        this.keyboardLayout = instance.getKeyboardLayout();
        this.cloudId = instance.getCloudId();
        this.computeId = instance.getComputeId();
    }

    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getComments() {
        return comments;
    }

    public InstanceState getState() {
        return state;
    }

    public PlanType getPlan() {
        return plan;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public String getUsername() {
        return username;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    @JsonIgnore
    public Long getCloudId() {
        return cloudId;
    }

    @JsonIgnore
    public String getComputeId() {
        return computeId;
    }
}
