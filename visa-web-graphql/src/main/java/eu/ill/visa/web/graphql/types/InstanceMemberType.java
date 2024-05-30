package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("InstanceMember")
public class InstanceMemberType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull UserType user;
    private final @NotNull InstanceMemberRole role;
    private final @NotNull Date createdAt;

    public InstanceMemberType(final InstanceMember member) {
        this.id = member.getId();
        this.user = new UserType(member.getUser());
        this.role = member.getRole();
        this.createdAt = member.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public UserType getUser() {
        return user;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
