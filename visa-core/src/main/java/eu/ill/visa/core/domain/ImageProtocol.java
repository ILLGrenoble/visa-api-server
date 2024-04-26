package eu.ill.visa.core.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonPropertyOrder({"id", "name", "port"})
@Entity
@Table(name = "image_protocol")
public class ImageProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "port", nullable = false)
    private Integer port;

    @Column(name = "optional", nullable = true)
    private Boolean optional;

    public ImageProtocol() {
    }

    public ImageProtocol(String name, Integer port) {
        this.name = name;
        this.port = port;
        this.optional = false;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean isOptional() {
        if (this.optional == null) {
            return false;
        } else {
            return this.optional;
        }
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ImageProtocol protocol = (ImageProtocol) o;

        return new EqualsBuilder()
            .append(id, protocol.name)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }

}
