package eu.ill.visa.web.graphqlxx.types;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class ImageProtocolType {

    @AdaptToScalar(Scalar.Int.class)
    private Long id;
    private String name;
    private Integer port;
    private Boolean optional;

    public ImageProtocolType() {
    }

    public ImageProtocolType(String name, Integer port) {
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

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }
}
