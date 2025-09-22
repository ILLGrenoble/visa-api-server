package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@Entity
@NamedQueries({
})
@Table(name = "flavour_device")
public class FlavourDevice {

    @EmbeddedId
    private FlavourDeviceKey id;

    @ManyToOne(optional = false)
    @MapsId("flavourId")
    @JoinColumn(name = "flavour_id", foreignKey = @ForeignKey(name = "fk_flavour_id"), nullable = false)
    private Flavour flavour;

    @ManyToOne(optional = false)
    @MapsId("devicePoolId")
    @JoinColumn(name = "device_pool_id", foreignKey = @ForeignKey(name = "fk_device_pool_id"), nullable = false)
    private DevicePool devicePool;

    @Column(name = "unit_count", nullable = false)
    private Integer unitCount;

    public FlavourDevice() {
    }

    public FlavourDevice(Flavour flavour, DevicePool devicePool, Integer unitCount) {
        this.id = new FlavourDeviceKey(flavour.getId(), devicePool.getId());
        this.flavour = flavour;
        this.devicePool = devicePool;
        this.unitCount = unitCount;
    }

    public FlavourDeviceKey getId() {
        return id;
    }

    public void setId(FlavourDeviceKey id) {
        this.id = id;
    }

    public Flavour getFlavour() {
        return flavour;
    }

    public void setFlavour(Flavour flavour) {
        this.flavour = flavour;
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

        FlavourDevice that = (FlavourDevice) o;

        return new EqualsBuilder()
            .append(flavour, that.flavour)
            .append(devicePool, that.devicePool)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(flavour)
            .append(devicePool)
            .toHashCode();
    }

    @Embeddable
    public static class FlavourDeviceKey implements Serializable {
        private Long flavourId;
        private Long devicePoolId;

        public FlavourDeviceKey() {
        }

        public FlavourDeviceKey(Long flavourId, Long devicePoolId) {
            this.flavourId = flavourId;
            this.devicePoolId = devicePoolId;
        }

        public Long getFlavourId() {
            return flavourId;
        }

        public void setFlavourId(Long flavourId) {
            this.flavourId = flavourId;
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

            FlavourDeviceKey that = (FlavourDeviceKey) o;

            return new EqualsBuilder()
                .append(flavourId, that.flavourId)
                .append(devicePoolId, that.devicePoolId)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(flavourId)
                .append(devicePoolId)
                .toHashCode();
        }
    }
}
