package eu.ill.visa.core.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonPropertyOrder({"id", "name", "port"})
@Entity
@NamedQueries({
    @NamedQuery(name = "imageProtocol.getById", query = """
            SELECT i
            FROM ImageProtocol i
            WHERE i.id = :id
    """),
    @NamedQuery(name = "imageProtocol.getByName", query = """
            SELECT i
            FROM ImageProtocol i
            WHERE i.name = :name
    """),
    @NamedQuery(name = "imageProtocol.getAll", query = """
            SELECT i
            FROM ImageProtocol i
            ORDER BY i.name ASC
    """),
})
@Table(name = "protocol")
public class ImageProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "port", nullable = false)
    private Integer port;

    public ImageProtocol() {
    }

    public ImageProtocol(String name, Integer port) {
        this.name = name;
        this.port = port;
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
