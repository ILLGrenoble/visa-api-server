package eu.ill.visa.web.dtos;

import eu.ill.visa.core.domain.ImageProtocol;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class ImageDto {

    @NotNull
    private Long id;

    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @NotNull
    @Size(min = 1, max = 100)
    private String version;

    @Size(max = 2500)
    private String description;


    @NotNull
    @Size(max = 100)
    private String icon;

    private List<ImageProtocol> protocols;

    public ImageDto() {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<ImageProtocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<ImageProtocol> protocols) {
        this.protocols = protocols;
    }
}
