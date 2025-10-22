package eu.ill.visa.web.graphql.inputs;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.graphql.Input;

import java.util.List;

@Input("FlavourInput")
public class FlavourInput {

    @Size(min = 1, max = 250)
    private @NotNull String name;
    private String description;
    @Min(1)
    private @NotNull Integer memory;
    private @NotNull Float cpu;
    private @AdaptToScalar(Scalar.Int.class) Long cloudId;
    private @NotNull String computeId;
    private @AdaptToScalar(Scalar.Int.class) List<Long> instrumentIds;
    private @AdaptToScalar(Scalar.Int.class) List<Long> roleIds;
    private List<RoleLifetimeInput> roleLifetimes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Float getCpu() {
        return cpu;
    }

    public void setCpu(Float cpu) {
        this.cpu = cpu;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public List<Long> getInstrumentIds() {
        return instrumentIds;
    }

    public void setInstrumentIds(List<Long> instrumentIds) {
        this.instrumentIds = instrumentIds;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public List<RoleLifetimeInput> getRoleLifetimes() {
        return roleLifetimes;
    }

    public void setRoleLifetimes(List<RoleLifetimeInput> roleLifetimes) {
        this.roleLifetimes = roleLifetimes;
    }

    public static final class RoleLifetimeInput {
        @AdaptToScalar(Scalar.Int.class) Long id;
        @AdaptToScalar(Scalar.Int.class) Long roleId;
        @AdaptToScalar(Scalar.Int.class) Long lifetimeMinutes;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public Long getLifetimeMinutes() {
            return lifetimeMinutes;
        }

        public void setLifetimeMinutes(Long lifetimeMinutes) {
            this.lifetimeMinutes = lifetimeMinutes;
        }
    }
}
