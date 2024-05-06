package eu.ill.visa.web.rest.converters.http;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.ImageService;
import eu.ill.visa.core.entity.Image;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.ParamConverter;

@ApplicationScoped
public class ImageParamConverter implements ParamConverter<Image> {

    private final ImageService imageService;

    @Inject
    public ImageParamConverter(final ImageService imageService) {
        this.imageService = imageService;
    }

    @Override
    public Image fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final Image image = imageService.getById(id);
            if (image == null) {
                throw new NotFoundException("Image not found");
            }
            return image;
        }
        throw new NotFoundException("Image not found");


    }

    @Override
    public String toString(final Image value) {
        return value.toString();
    }
}
