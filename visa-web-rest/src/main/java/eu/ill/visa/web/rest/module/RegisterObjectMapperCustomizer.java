package eu.ill.visa.web.rest.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.context.ApplicationScoped;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@ApplicationScoped
public class RegisterObjectMapperCustomizer implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JodaModule());
        objectMapper.setSerializationInclusion(NON_NULL);
        objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.enable(STRICT_DUPLICATE_DETECTION);
//        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }
}
