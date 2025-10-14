package eu.ill.visa.core.entity.converter;

import eu.ill.visa.core.domain.SimpleDuration;
import jakarta.persistence.AttributeConverter;

public class SimpleDurationConverter implements AttributeConverter<SimpleDuration, Long> {

    @Override
    public Long convertToDatabaseColumn(SimpleDuration simpleDuration) {
        if (simpleDuration == null) {
            return null;
        }
        return simpleDuration.getDuration().toMinutes();
    }

    @Override
    public SimpleDuration convertToEntityAttribute(Long data) {
        return new SimpleDuration(data);
    }
}
