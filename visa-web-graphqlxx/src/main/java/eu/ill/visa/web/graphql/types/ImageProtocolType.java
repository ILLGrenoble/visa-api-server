package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.ImageProtocol;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

@Type("ImageProtocol")
public class ImageProtocolType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;
    private final Integer port;
    private final Boolean optional;

    public ImageProtocolType(final ImageProtocol imageProtocol) {
        this.id = imageProtocol.getId();
        this.name = imageProtocol.getName();
        this.port = imageProtocol.getPort();
        this.optional = imageProtocol.isOptional();
    }

    public ImageProtocolType(String name, Integer port) {
        this.id = null;
        this.name = name;
        this.port = port;
        this.optional = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getPort() {
        return port;
    }

    public Boolean getOptional() {
        return optional;
    }
}
