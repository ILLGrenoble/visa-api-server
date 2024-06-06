package eu.ill.visa.cloud.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

public class CloudInstance {

    protected CloudInstanceState state;
    private String id;
    private String name;
    private String address;
    private String imageId;
    private String flavorId;
    private Date createdAt;
    private CloudInstanceFault fault;
    private List<String> securityGroups;
    private CloudInstanceMetadata metadata;
    private String bootCommand;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CloudInstanceState getState() {
        return state;
    }

    public void setState(CloudInstanceState state) {
        this.state = state;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CloudInstanceFault getFault() {
        return fault;
    }

    public void setFault(CloudInstanceFault fault) {
        this.fault = fault;
    }

    public Boolean isStatus(CloudInstanceState... statuses) {
        for (final CloudInstanceState s : statuses) {
            if (this.state.equals(state)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isName(String name) {
        return this.name.equals(name);
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public CloudInstanceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(CloudInstanceMetadata metadata) {
        this.metadata = metadata;
    }

    public String getBootCommand() {
        return bootCommand;
    }

    public void setBootCommand(String bootCommand) {
        this.bootCommand = bootCommand;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CloudInstance) {
            final CloudInstance other = (CloudInstance) object;
            return new EqualsBuilder()
                .append(name, other.name)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(name)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("createdAt", createdAt)
            .append("state", state)
            .append("address", address)
            .append("flavorId", flavorId)
            .append("imageId", imageId)
            .toString();
    }

    public static Builder newBuilder() {
        return new CloudInstance.Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private String address;
        private String imageId;
        private String flavorId;
        private Date createdAt;
        protected CloudInstanceState status;
        private CloudInstanceFault fault;
        private List<String> securityGroups;
        private CloudInstanceMetadata metadata;
        private String bootCommand;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder state(CloudInstanceState status) {
            this.status = status;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder imageId(String imageId) {
            this.imageId = imageId;
            return this;
        }

        public Builder flavorId(String flavorId) {
            this.flavorId = flavorId;
            return this;
        }

        public Builder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder fault(CloudInstanceFault fault) {
            this.fault = fault;
            return this;
        }

        public Builder securityGroups(List<String> securityGroups) {
            this.securityGroups = securityGroups;
            return this;
        }

        public Builder metadata(CloudInstanceMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder bootCommand(String bootCommand) {
            this.bootCommand = bootCommand;
            return this;
        }

        public CloudInstance build() {
            final CloudInstance cloudInstance = new CloudInstance();
            cloudInstance.setState(status);
            cloudInstance.setId(id);
            cloudInstance.setName(name);
            cloudInstance.setAddress(address);
            cloudInstance.setImageId(imageId);
            cloudInstance.setFlavorId(flavorId);
            cloudInstance.setCreatedAt(createdAt);
            cloudInstance.setFault(fault);
            cloudInstance.setSecurityGroups(securityGroups);
            cloudInstance.setMetadata(metadata);
            cloudInstance.setBootCommand(bootCommand);
            return cloudInstance;
        }
    }
}
