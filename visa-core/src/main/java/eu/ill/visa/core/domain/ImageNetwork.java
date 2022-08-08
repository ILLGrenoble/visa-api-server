package eu.ill.visa.core.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonPropertyOrder({"id", "networkId", "external"})
public class ImageNetwork {

    private Long   id;
    private String networkId;

    private boolean external;

    public ImageNetwork() {

    }

    public ImageNetwork(final String networkId) {
        this.networkId = networkId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ImageNetwork network = (ImageNetwork) o;

        return new EqualsBuilder()
            .append(networkId, network.networkId)
            .isEquals();
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(networkId)
            .toHashCode();
    }

}
