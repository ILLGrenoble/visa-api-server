package eu.ill.visa.business.services;

import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.core.entity.Image;
import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.persistence.repositories.ImageRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class ImageService {

    private final ImageRepository repository;
    private final String defaultVdiProtocol;

    @Inject
    public ImageService(ImageRepository repository, InstanceConfiguration instanceConfiguration) {
        this.repository = repository;
        this.defaultVdiProtocol = instanceConfiguration.defaultVdiProtocol();
    }

    public List<Image> getAll() {
        return this.repository.getAll();
    }

    public Image getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(Image image) {
        this.repository.delete(image);
    }

    public void save(@NotNull Image image) {
        this.repository.save(image);
    }

    public List<Image> getAllForAdmin() {
        return this.repository.getAllForAdmin();
    }

    public Long countAllForAdmin() {
        return this.repository.countAllForAdmin();
    }

    public ImageProtocol getDefaultVdiProtocolForImage(Image image) {
        if (image.getDefaultVdiProtocol() != null) {
            return image.getDefaultVdiProtocol();
        }

        final ImageProtocol guacamoleProtocol = image.getProtocols().stream().filter(imageProtocol -> imageProtocol.getName().equals("GUACD")).findFirst().orElse(null);
        final ImageProtocol webXProtocol = image.getProtocols().stream().filter(imageProtocol -> imageProtocol.getName().equals("WEBX")).findFirst().orElse(null);

        if (this.defaultVdiProtocol.equals("GUACD")) {
            return (guacamoleProtocol != null) ? guacamoleProtocol : webXProtocol;

        } else {
            return (webXProtocol != null) ? webXProtocol : guacamoleProtocol;
        }
    }
}
