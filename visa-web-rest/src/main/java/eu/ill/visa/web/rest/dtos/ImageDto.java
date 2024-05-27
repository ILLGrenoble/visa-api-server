package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Image;
import eu.ill.visa.core.entity.ImageProtocol;

import java.util.List;

public class ImageDto {

    private final Long id;
    private final String name;
    private final String version;
    private final String description;
    private final String icon;
    private final List<ImageProtocol> protocols;

    public ImageDto(final Image image) {
        this.id = image.getId();
        this.name = image.getName();
        this.version = image.getVersion();
        this.description = image.getDescription();
        this.icon = image.getIcon();
        this.protocols = image.getProtocols();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public List<ImageProtocol> getProtocols() {
        return protocols;
    }
}
