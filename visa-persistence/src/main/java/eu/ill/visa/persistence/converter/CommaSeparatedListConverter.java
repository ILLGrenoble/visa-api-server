package eu.ill.visa.persistence.converter;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommaSeparatedListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> values) {
        if (values == null) {
            return null;
        }
        String valueString = StringUtils.join(values,",");
        return valueString;
    }

    @Override
    public List<String> convertToEntityAttribute(String data) {
        if (StringUtils.isBlank(data)) {
            return Collections.emptyList();
        }

        return Arrays.asList(data.split(","));
    }
}
