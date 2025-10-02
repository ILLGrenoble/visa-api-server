package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceDeviceAllocation.getAll", query = """
        SELECT a FROM InstanceDeviceAllocation a
        JOIN Instance i ON a.instance.id = i.id
        WHERE i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceDeviceAllocation.getAllByInstanceId", query = """
        SELECT a FROM InstanceDeviceAllocation a
        JOIN Instance i ON a.instance.id = i.id
        WHERE i.id = :instanceId
    """),
    @NamedQuery(name = "instanceDeviceAllocation.getAllByInstanceIds", query = """
        SELECT a FROM InstanceDeviceAllocation a
        JOIN Instance i ON a.instance.id = i.id
        WHERE i.id IN :instanceIds
    """),
})
@Table(name = "instance_device_allocation")
public class InstanceDeviceAllocation {

    @EmbeddedId
    private InstanceDeviceKey id;

    @ManyToOne(optional = false)
    @MapsId("instanceId")
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    @ManyToOne(optional = false)
    @MapsId("devicePoolId")
    @JoinColumn(name = "device_pool_id", foreignKey = @ForeignKey(name = "fk_device_pool_id"), nullable = false)
    private DevicePool devicePool;

    @Column(name = "unit_count", nullable = false)
    private Integer unitCount;

    public InstanceDeviceAllocation() {
    }

    public InstanceDeviceAllocation(Instance instance, DevicePool devicePool, Integer unitCount) {
        this.id = new InstanceDeviceKey(instance.getId(), devicePool.getId());
        this.instance = instance;
        this.devicePool = devicePool;
        this.unitCount = unitCount;
    }

    public InstanceDeviceKey getId() {
        return id;
    }

    public void setId(InstanceDeviceKey id) {
        this.id = id;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public DevicePool getDevicePool() {
        return devicePool;
    }

    public void setDevicePool(DevicePool devicePool) {
        this.devicePool = devicePool;
    }

    public Integer getUnitCount() {
        return unitCount;
    }

    public void setUnitCount(Integer unitCount) {
        this.unitCount = unitCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceDeviceAllocation that = (InstanceDeviceAllocation) o;

        return new EqualsBuilder()
            .append(instance, that.instance)
            .append(devicePool, that.devicePool)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(instance)
            .append(devicePool)
            .toHashCode();
    }

    @Embeddable
    public static class InstanceDeviceKey implements Serializable {
        private Long instanceId;
        private Long devicePoolId;

        public InstanceDeviceKey() {
        }

        public InstanceDeviceKey(Long instanceId, Long devicePoolId) {
            this.instanceId = instanceId;
            this.devicePoolId = devicePoolId;
        }

        public Long getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
        }

        public Long getDevicePoolId() {
            return devicePoolId;
        }

        public void setDevicePoolId(Long devicePoolId) {
            this.devicePoolId = devicePoolId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            InstanceDeviceKey that = (InstanceDeviceKey) o;

            return new EqualsBuilder()
                .append(instanceId, that.instanceId)
                .append(devicePoolId, that.devicePoolId)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(instanceId)
                .append(devicePoolId)
                .toHashCode();
        }
    }
}
