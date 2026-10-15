package eu.ill.visa.common.vdi.domain;

import eu.ill.visa.common.vdi.domain.enums.InstanceMemberRole;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Objects;

@RegisterForReflection
public record ConnectedUser(String id, String fullName, InstanceMemberRole role) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectedUser that = (ConnectedUser) o;
        return Objects.equals(id, that.id) && Objects.equals(fullName, that.fullName) && role == that.role;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(fullName);
        result = 31 * result + Objects.hashCode(role);
        return result;
    }

    @Override
    public String toString() {
        return this.fullName + " (" + this.id + ", " + this.role + ")";
    }
}
