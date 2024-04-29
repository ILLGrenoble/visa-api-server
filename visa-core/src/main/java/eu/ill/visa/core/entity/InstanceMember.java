package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "instance_member")
public class InstanceMember extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private InstanceMemberRole role;

    private InstanceMember(Builder builder) {
        this.user = builder.user;
        this.role = builder.role;
    }

    public InstanceMember() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isUser(User user) {
        return this.user.equals(user);
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public boolean isRole(InstanceMemberRole role) {
        return this.role == role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof InstanceMember) {
            final InstanceMember other = (InstanceMember) object;
            return new EqualsBuilder()
                .append(id, other.id)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("role", role)
            .append("createdAt", createdAt)
            .append("updatedAt", updatedAt)
            .append("user", user)
            .toString();
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private User user;
        private InstanceMemberRole role;

        private Builder() {
        }


        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder role(InstanceMemberRole role) {
            this.role = role;
            return this;
        }

        public InstanceMember build() {
            InstanceMember instanceMember = new InstanceMember();
            instanceMember.setId(id);
            instanceMember.setUser(user);
            instanceMember.setRole(role);
            return instanceMember;
        }
    }
}
