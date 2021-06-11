package eu.ill.visa.persistence.listeners;

import eu.ill.visa.core.domain.Timestampable;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

public class TimestampableEntityListener {

    @PrePersist
    public void onCreate(final Object entity) {
        if (entity instanceof Timestampable) {
            final Timestampable entityData = (Timestampable) entity;
            entityData.setCreatedAt(new Date());
            entityData.setUpdatedAt(new Date());
        }
    }

    @PreUpdate
    public void onPersist(final Object entity) {
        if (entity instanceof Timestampable) {
            final Timestampable entityData = (Timestampable) entity;
            entityData.setUpdatedAt(new Date());
        }
    }

}
