package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.domain.Image;

import jakarta.persistence.EntityManager;

public class ImageFilterProvider extends AbstractFilterQueryProvider<Image> {

    public ImageFilterProvider(EntityManager entityManager) {
        super(Image.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("name"),
            orderableField("version"),
            field("description"),
            field("icon"),
            orderableField("computeId")
        );
    }

}
